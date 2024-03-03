pipeline {
    agent none

    environment {
        DOCKERHUB_CREDENTIALS = credentials('kirilljbee_dockerhub')
        NAME_IMAGE_DEV = 'kirilljbee/testfluskapp:dev'
        NAME_CONTAINER_DEV = 'testfluskapp_dev'
        TAG_IMAGE_PROD = 'prod'

    }

    stages {

        stage('build devimage') { 
            agent { 
                label 'awsssh'
            }   
            
            steps {
                sh 'docker build -t ${NAME_IMAGE_DEV} .'    
            }
        }

        stage('push devimage') {
            agent { label 'awsssh'} 

            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push ${NAME_IMAGE_DEV}'
                sh 'docker rmi ${NAME_IMAGE_DEV}'
                //Удаляем рабочие директории проекта
                cleanWs()
                    dir("${env.WORKSPACE}@tmp") {
                        deleteDir()
                    }
            }
        }  
        
        stage('test devimage & push prodimage') {
            agent { label 'PQHssh'} 

            steps {
                script {
                    sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                    sh 'docker pull ${NAME_IMAGE_DEV}'
                    sh 'docker run -d --rm -p 8000:8000 --name ${NAME_CONTAINER_CONT} ${NAME_IMAGE_DEV}'
                    sh 'ping -c 5 localhost'

                    sh 'curl http://localhost:8000'

                    docker.image("${NAME_IMAGE_DEV}").tag("${TAG_IMAGE_PROD}")
                    docker.image("${NAME_IMAGE_DEV}").push("${TAG_IMAGE_PROD}")

                    sh 'timeout 20s docker stop ${NAME_CONTAINER_DEV)'

                    //sh 'docker rmi ${NAME_IMAGE_DEV)'
                }
            } 
        }   
        
        // stage('deploy production') {
        //     agent { label 'PQHssh'}
            
        //     input {
        //             message "Ready to deploy?"
        //             ok "Yes"
        //         }

        //     steps {
        //         sh 'ansible-playbook playbook.yml -i hosts.ini'
        //         //sh 'ansible all -i hosts.ini -m ping'
        //         //sh 'ansible-playbook playbook.yml'
        //         cleanWs()
        //             dir("${env.WORKSPACE}@tmp") {
        //                 deleteDir()
        //             }
        //              dir("${env.WORKSPACE}@script") {
        //                  deleteDir()
        //             }
        //             dir("${env.WORKSPACE}@script@tmp") {
        //                 deleteDir()
        //             }
        //     }
        // }
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

