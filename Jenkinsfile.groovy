pipeline {
    agent none

    environment {
        DOCKERHUB_CREDENTIALS = credentials('kirilljbee_dockerhub')
    }

    stages {

        stage('build docker image') { 
            agent { 
                label 'awsssh'
            }   
            
            steps {
                sh 'docker build -t kirilljbee/testfluskapp:test .'    
            }
        }

        stage('push docker image') {
            agent { label 'awsssh'} 

            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push kirilljbee/testfluskapp:test'
                //sh 'docker stop $(docker ps -a -q)'
                sh 'docker system prune -af'
                cleanWs()
                    dir("${env.WORKSPACE}@tmp") {
                        deleteDir()
                    }
                     dir("${env.WORKSPACE}@script") {
                         deleteDir()
                    }
                    dir("${env.WORKSPACE}@script@tmp") {
                        deleteDir()
                    }
            }
        }  
        
        stage('run test docker image & push prod docker image') {
            agent { label 'PQHssh'} 

            steps {
                script {
                    sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                    sh 'docker pull kirilljbee/testfluskapp:test'
                    sh 'docker run -d --rm -p 8000:8000 kirilljbee/testfluskapp:test'
                    sh 'ping -c 5 localhost'

                    sh 'curl http://localhost:8000'

                    //docker.image('kirilljbee/testfluskapp:test').tag("${BUILD_NUMBER}")
                    //docker.image('kirilljbee/testfluskapp:test').push("${BUILD_NUMBER}")

                    docker.image('kirilljbee/testfluskapp:test').tag("latest")
                    docker.image('kirilljbee/testfluskapp:test').push("latest")

                    sh 'docker stop $(docker ps -a -q)'
                    sh 'docker system prune -af'

                    
                }
            } 
        }   
        
        stage('deploy production') {
            agent { label 'PQHssh'}
            
            steps {
                sh 'ansible --version'
                sh 'ansible-playbook playbook.yml -i hosts.ini'
                //sh 'ansible all -i hosts.ini -m ping'
                //sh 'ansible-playbook playbook.yml'
                cleanWs()
                    dir("${env.WORKSPACE}@tmp") {
                        deleteDir()
                    }
                     dir("${env.WORKSPACE}@script") {
                         deleteDir()
                    }
                    dir("${env.WORKSPACE}@script@tmp") {
                        deleteDir()
                    }
            }
        
        }
    }

    post { 

            success {
                mail to: 'jbeework@gmail.com',
                subject: "Job '${JOB_NAME}' (${BUILD_NUMBER}) was successfully completed!",
                body: "Please go to ${BUILD_URL} and verify the build"      
            }

            failure {
                mail to: 'jbeework@gmail.com',
                subject: "Job '${JOB_NAME}' (${BUILD_NUMBER}) ended unsuccessfully!",
                body: "Please go to ${BUILD_URL} and verify the build"              
            }

            aborted {
                mail to: 'jbeework@gmail.com',
                subject: "Job '${JOB_NAME}' (${BUILD_NUMBER}) was aborted",
                body: "Please go to ${BUILD_URL} and verify the build" 
            }
        }
}

