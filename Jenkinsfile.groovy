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
                sh 'docker run -d --rm -p 8000:8000 kirilljbee/testfluskapp:latest'
            }
        } 

        stage('test docker image') {
            agent { label 'PQHssh'} 

            steps {
                sh 'curl http://localhost:8000/'
            }
        }

    }


    post { 

        // Clean after build
        always {
            cleanWs(cleanWhenNotBuilt: false,
                    deleteDirs: true,
                    disableDeferredWipeout: true,
                    notFailBuild: true,
                    patterns: [[pattern: '.gitignore', type: 'INCLUDE'],
                               [pattern: '.propsfile', type: 'EXCLUDE']])
        }

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

       

    }


}

