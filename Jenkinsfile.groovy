pipeline {
    agent none

    environment {
        DOCKERHUB_CREDENTIALS = credentials('kirilljbee_dockerhub')

    }

    stages {

        stage('build devimage') { 
            agent { 
                label 'awsssh'
            }   
            
            steps {
                sh 'docker build -t kirilljbee/testfluskapp:dev .'    
            }
        }

        stage('push devimage') {
            agent { label 'awsssh'} 

            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push kirilljbee/testfluskapp:dev'
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
        
        stage('test devimage & push prodimage') {
            agent { label 'PQHssh'} 

            steps {
                script {
                    sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                    sh 'docker pull kirilljbee/testfluskapp:dev'
                    sh 'docker run -d --rm -p 8000:8000 kirilljbee/testfluskapp:dev'
                    sh 'ping -c 5 localhost'

                    sh 'curl http://localhost:8000'

                    //docker.image('kirilljbee/testfluskapp:test').tag("${BUILD_NUMBER}")
                    //docker.image('kirilljbee/testfluskapp:test').push("${BUILD_NUMBER}")

                    docker.image('kirilljbee/testfluskapp:dev').tag("prod")
                    docker.image('kirilljbee/testfluskapp:dev').push("prod")

                    sh 'docker stop $(docker ps -a -q)'
                    sh 'docker system prune -af'

                    
                }
            } 
        }   
        
        stage('deploy production') {
            agent { label 'PQHssh'}
            
            input {
                    message "Ready to deploy?"
                    ok "Yes"
                }
            steps {
				ansiblePlaybook playbook: 'playbook.yml', inventory: 'hosts.ini'
                
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

