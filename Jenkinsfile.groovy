pipeline {

    agent {
        label "awsssh"
    }

    stages {
        stage('example') {
            steps {
                echo "Hello world"
            }
        }
    }
     
    post {
        success {
            mail body: 'Success',
                     subject: 'Test Subject',
                     to: 'jbeework@gmail.com'
        }

        failure {
            mail body: 'Сборка прошла неуспешно. Обратите внимание',
                     subject: 'kork',
                     to: 'jbeework@gmail.com'
        }
    }
}

