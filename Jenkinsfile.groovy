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
        
        stage('run test deploy docker image') {
            agent { label 'PQHssh'} 

            steps {
                script {
                    sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                    sh 'docker pull kirilljbee/testfluskapp:test'
                    sh 'docker run -d --rm -p 8000:8000 kirilljbee/testfluskapp:test'
                    sh 'ping -c 5 localhost'
                    sh 'curl http://localhost:8000'
                    docker.image('kirilljbee/testfluskapp:test').tag('prod')
                    docker.image('kirilljbee/testfluskapp:test').push('prod')
                    sh 'docker stop $(docker ps -a -q)'
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

       

    }


}

