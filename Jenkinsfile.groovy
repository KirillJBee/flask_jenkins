pipeline {

        stages {
        stage('build docker image') {
            agent {
                label 'awsssh'
            }

            steps {
                sh "docker build  -t ${DOCKER_IMAGE_NAME} ." 
            }
        }

        //stage('test') {
        //   steps {
        //        sh "docker run -d --rm testfluskapp:V1.0"
        //        sh "curl http://172.0.0.1:8000"
        //    }
        //}


        
    }

    post { 

        success {
            mail body: 'Сборка прошла успешно. Наши поздравления!',
                     subject: 'Успешная сборка',
                     to: 'jbeework@gmail.com'
        }

        failure {
            mail body: 'Сборка прошла неуспешно. Обратите внимание!',
                     subject: 'Неуспешная сборка',
                     to: 'jbeework@gmail.com'
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

