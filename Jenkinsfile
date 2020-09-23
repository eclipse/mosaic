pipeline {
    agent 'any'

    tools {
        maven 'Maven'
        jdk 'JDK8'
    }

    stages {
        stage('Build') {
            steps {
                withMaven(jdk: 'JDK8', maven: 'Maven', mavenLocalRepo: '.repository', publisherStrategy: 'EXPLICIT') {
                    sh 'mvn clean install -DskipTests -fae -T 4'
                }
            }
        }

        stage('Test') {
            steps {
                withMaven(jdk: 'JDK8', maven: 'Maven', mavenLocalRepo: '.repository', publisherStrategy: 'EXPLICIT') {
                    sh 'mvn test -fae -T 4'
                }
            }

            post {
                always {
                    junit '**/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                withMaven(jdk: 'JDK8', maven: 'Maven', mavenLocalRepo: '.repository', publisherStrategy: 'EXPLICIT') {
                    sh 'mvn test -fae -P integration-tests'
                }
            }

            post {
                always {
                    junit 'test/**/surefire-reports/*.xml'
                }
            }
        }

        stage('Analysis') {
            steps {
                withMaven(jdk: 'JDK8', maven: 'Maven', mavenLocalRepo: '.repository', publisherStrategy: 'EXPLICIT') {
                    sh 'mvn site -T 4'
                }
            }

            post {
                always {
                    recordIssues(sourceCodeEncoding: 'UTF-8', tools: [
                            spotBugs(),
                            checkStyle(),
                            taskScanner(highTags: 'FIXME', normalTags: 'TODO', ignoreCase: true, includePattern: '**/*.java')
                    ])
                }
            }
        }

        stage('Deploy') {
            when {
                expression { env.BRANCH_NAME == 'master' }
            }
            steps {
                withMaven(jdk: 'JDK8', maven: 'Maven', mavenLocalRepo: '.repository', publisherStrategy: 'EXPLICIT') {
                    sh 'mvn deploy'
                }
            }
        }
    }

    post {
        success {
            cleanWs()
        }
        fixed {
            sendMail("Successful")
        }
        unstable {
            sendMail("Unstable")
        }
        failure {
            sendMail("Failed")
        }
    }
}

def sendMail(status) {
    emailext(
            recipientProviders: [culprits()],
            subject: status + ": ${currentBuild.fullDisplayName}",
            body: '${JELLY_SCRIPT, template="html-with-health-and-console"}',
            mimeType: 'text/html'
    )
}