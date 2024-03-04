pipeline {
    agent none

    environment {
        NAME_PROJECT = 'testfluskapp'
        NAME_IMAGE_DEV = 'kirilljbee/testfluskapp:dev'
        NAME_CONTAINER_DEV = 'testfluskapp_dev'
        TAG_IMAGE_PROD = 'prod'
        
    }

    stages {

        // stage('build devimage') { 
        //     agent { 
        //         label 'awsssh'
        //     }   
            
        //     steps {
        //         sh 'docker build -t ${NAME_IMAGE_DEV} .'    
        //     }
        // }

        // stage('push devimage') {
        //     agent { label 'awsssh'} 

        //     steps {
        //         sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
        //         sh 'docker push ${NAME_IMAGE_DEV}'
        //         sh 'docker rmi ${NAME_IMAGE_DEV}'
        //         //Удаляем рабочие директории проекта
        //         cleanWs()
        //             dir("${env.WORKSPACE}@tmp") {
        //                 deleteDir()
        //             }
        //     }
        // }  
        
        // stage('test devimage & push prodimage') {
        //     agent { label 'PQHssh'} 

        //     steps {
        //         script {
        //             sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
        //             sh 'docker pull ${NAME_IMAGE_DEV}'
        //             sh 'docker run -d --name ${NAME_CONTAINER_DEV} -d --rm -p 8000:8000 ${NAME_IMAGE_DEV}'
        //             sh 'ping -c 5 localhost'

        //             sh 'curl http://localhost:8000'

        //             docker.image("${NAME_IMAGE_DEV}").tag("${TAG_IMAGE_PROD}")
        //             docker.image("${NAME_IMAGE_DEV}").push("${TAG_IMAGE_PROD}")

        //             sh 'docker stop -t 5 ${NAME_CONTAINER_DEV}'
        //             sh 'docker system prune -af' 
        //         }
        //     } 
        // }   
        
        stage('deploy production') {
            agent { label 'PQHssh' }
            

            steps {
                script {
                    withCredentials([file(credentialsId: 'key_to_prod_server', variable: 'KEY_SERVER_PROD')]) {
                        //sh 'cat $KEY_SERVER_PROD'
                        sh ('ansible all -i inventory --connection-password-file $KEY_SERVER_PROD')
               
                    }
                }
            }
        }



    }
    
}

