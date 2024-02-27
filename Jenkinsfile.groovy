pipeline {

    environment {
        DOCKERHUB_CREDENTIALS = credentials('kirilljbee_dockerhub')
    
    }

    agent none
    
    stages {

        stage('build docker image') {
            agent { label 'awsssh'}   
            
            steps {
                sh 'docker build -t kirilljbee/testfluskapp:latest .'    
            }
        }

        stage('push docker image') {
            agent { label 'awsssh'} 

            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push kirilljbee/testfluskapp:latest'
            }
        }  
        
        stage('deploy docker image') {
            agent { label 'PQHssh'} 

            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker pull kirilljbee/testfluskapp:latest'
                sh 'docker-compose -f -d compose.yaml'
            }
        } 

        stage('test deploy image') {
            agent { label 'PQHssh'} 
            
            steps {
                sh 'docker pull kirilljbee/testfluskapp:latest'

            }
        }

    }


    post { 

        success {
            mail body: 'Сборка прошла успешно. Наши поздравления!',
                     subject: 'Успешная сборка',
                     to: 'jbeework@gmail.com'
        }

        failure {
            mail to: 'jbeework@gmail.com', subject: 'The Pipeline failed :(', body: 'empty'
                            
        }

        aborted {
            mail body: 'Сборка была прервана! Обратите внимание!',
                     subject: 'Прерванная сборка',
                     to: 'jbeework@gmail.com'
        }

        always {

            //sh 'docker logout'

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

