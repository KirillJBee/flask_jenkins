pipeline {

    agent {
        label 'awsssh'
    }

    stages {
        stage('build') {
            steps {
                sh "docker build . -t testfluskapp:V1.0" 
            }
        }

        stage('deploy') {
            steps {
                sh "docker run -d --rm testfluskapp:V1.0"
            }
        }


        stage('test') {
            steps {
                sh "curl http://172.0.0.1:8000"
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
            mail body: 'Сборка прошла неуспешно. Обратите внимание!',
                     subject: 'Неуспешная сборка',
                     to: 'jbeework@gmail.com'
        }
    }
}

