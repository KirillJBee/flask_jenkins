pipeline {
    agent none

    environment {
        NAME_PROJECT = 'testfluskapp'
        DOCKERHUB_CREDENTIALS = credentials('kirilljbee_dockerhub')
        KEY_PROD = credentials ('passprodkey')
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
            agent { label 'PQHssh'}
            
            // input {
            //         message "Ready to deploy?"
            //         ok "Yes"
            //     }

            steps {
                sh 'ansible all -i inventory -m ping --connection-password-file $KEY_PROD'
             
                //sh 'ansible-playbook playbook.yml'
                
            }
        }
    }

    
}

