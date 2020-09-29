pipeline {
    agent {
        kubernetes {
            label 'mosaic-ci-pod'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.6.3-adoptopenjdk-8
    command:
    - cat
    tty: true
    resources:
      limits:
        memory: "2Gi"
        cpu: "1"
      requests:
        memory: "2Gi"
        cpu: "1"
  - name: maven-sumo
    image: eclipsemosaic/mosaic-ci:jdk8-sumo-1.7.0
    command:
    - cat
    tty: true
    resources:
      limits:
        memory: "2Gi"
        cpu: "1"
      requests:
        memory: "2Gi"
        cpu: "1"
"""
        }
    }
    stages {
        stage('Build') {
            steps {
                container('maven') {
                    sh 'mvn clean install -DskipTests -fae -T 4'
                }
            }
        }

        stage('Test') {
            steps {
                container('maven-sumo') {
                    sh 'mvn test -fae -T 4 -P coverage'
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
                container('maven-sumo') {
                    sh 'mvn test -fae -P integration-tests,coverage'
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
                container('maven') {
                    sh 'mvn site -T 4'
                }
            }

            post {
                always {
                    jacoco exclusionPattern: '**/ClientServerChannelProtos*.class', skipCopyOfSrcFiles: true, sourceExclusionPattern: '**/*.*', sourceInclusionPattern: '', sourcePattern: 'x'
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
                expression { env.BRANCH_NAME == 'main' }
            }
            steps {
                container('maven') {
                    sh 'mvn deploy -DskipTests'
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