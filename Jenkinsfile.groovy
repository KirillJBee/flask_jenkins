pipeline {
    agent none
    
    stages {
        stage('build docker image') {
            agent {
                label 'awsssh'
            }

            steps {
                sh "docker build -t testfluskapp:V2.0 ." 
            }
        }

        stage('push docker image') {
            agent {
                label 'awsssh'
            }

            steps {
                sh "docker build -t testfluskapp:V2.0 ." 
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

