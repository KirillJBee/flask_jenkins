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

     
    post {
        success {
            mail body: 'Success',
                     subject: 'Test Subject',
                     to: 'jbeework@gmail.com'
        }

        failure {
            mail body: 'Failure',
                     subject: 'Test Subject',
                     to: 'jbeework@gmail.com'
        }
    }
}
