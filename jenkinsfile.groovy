pipeline {
    agent awsssh

    stages {
        stage('build') {
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
