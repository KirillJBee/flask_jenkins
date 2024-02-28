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
                sh 'docker build -t kirilljbee/testfluskapp:latest .'    
            }
        }

        stage('push docker image') {
            agent { label 'awsssh'} 

            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push kirilljbee/testfluskapp:latest'
                //sh 'docker stop $(docker ps -a -q)'
                sh 'docker system prune -af'
            }
        }  
        
        stage('run test deploy docker image') {
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
                script {
                    // Perform tests
                    def maxRetries = 3
                    def retryCount = 0
                    def exitCode = -1

                    while (retryCount < maxRetries && exitcode != 0) {
                        retryCount++
                        echo "Attempt ${retryCount} to execute curl..."
                        exitCode = sh(returnStatus: true, script: 'curl http://localhost:8000/')                   
                    }

                    if (exitCode != 0) {
                        error "failed to execute after ${maxRetries} attempts."
                    }
                }
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

