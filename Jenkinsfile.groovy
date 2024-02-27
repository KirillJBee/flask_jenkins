pipeline {

    environment {
        DOCKERHUB_CREDENTIALS = credentials('kirilljbee_dockerhub')
    
    }

    agent none
    
    stages {

        agent {
                label 'awsssh'
            }

        stage('build docker image') {         
            steps {
                sh 'docker build -t kirilljbee/testfluskapp:latest .'    
            }
        }

        stage('push docker image') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push kirilljbee/testfluskapp:latest'
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
            mail to: 'jbeework@gmail.com', subject: 'The Pipeline failed :(', body: ''
                            
        }

        aborted {
            mail body: 'Сборка была прервана! Обратите внимание!',
                     subject: 'Прерванная сборка',
                     to: 'jbeework@gmail.com'
        }

        always {

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

