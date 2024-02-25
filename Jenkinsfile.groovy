pipeline {
    agent label {'awsssh'
    }

    stages {
        stage('Example') {
            step {
                echo 'Hello World'
            }}
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
