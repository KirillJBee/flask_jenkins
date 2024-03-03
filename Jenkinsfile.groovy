pipeline {
    agent { label 'PQHssh'}
    
    stages {

        stage('build docker image') {

            steps {
                sh 'docker build -t kirilljbee/testfluskapp:latest .'    
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

            // cleanWs()
            // dir("${env.WORKSPACE}@tmp") {
            // deleteDir()
            // }

            // dir("${env.WORKSPACE}@script") {
            //     deleteDir()
            // }  

            // dir("${env.WORKSPACE}@script@tmp") {
            //     deleteDir()
            // }
        }

    }


}

