pipeline {
    agent {
        kubernetes {
            label 'mosaic-ci-pod'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven-sumo
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
    volumeMounts:
    - name: settings-xml
      mountPath: /home/jenkins/.m2/settings.xml
      subPath: settings.xml
      readOnly: true
    - name: toolchains-xml
      mountPath: /home/jenkins/.m2/toolchains.xml
      subPath: toolchains.xml
      readOnly: true
    - name: settings-security-xml
      mountPath: /home/jenkins/.m2/settings-security.xml
      subPath: settings-security.xml
      readOnly: true
    - name: m2-repo
      mountPath: /home/jenkins/.m2/repository
    - name: volume-known-hosts
      mountPath: /home/jenkins/.ssh
  volumes:
  - name: settings-xml
    secret:
      secretName: m2-secret-dir
      items:
      - key: settings.xml
        path: settings.xml
  - name: toolchains-xml
    configMap:
      name: m2-dir
      items:
      - key: toolchains.xml
        path: toolchains.xml
  - name: settings-security-xml
    secret:
      secretName: m2-secret-dir
      items:
      - key: settings-security.xml
        path: settings-security.xml
  - name: m2-repo
    emptyDir: {}
  - name: volume-known-hosts
    configMap:
      name: known-hosts
"""
        }
    }
    stages {
        stage('Build') {
            steps {
                container('maven-sumo') {
                    sh 'mvn clean install -DskipTests -fae -T 4'
                }
            }
        }

        stage('Test') {
            steps {
                container('maven-sumo') {
                    sh 'echo "skip"'//sh 'mvn test -fae -T 4 -P coverage'
                }
            }

            post {
                always {
                    sh 'echo "skip"'//junit '**/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                container('maven-sumo') {
                    sh 'echo "skip"'//sh 'mvn test -fae -P integration-tests,coverage'
                }
            }

            post {
                always {
                    sh 'echo "skip"'//junit 'test/**/surefire-reports/*.xml'
                }
            }
        }

        stage('Analysis') {
            steps {
                container('maven-sumo') {
                    sh 'echo "skip"'//sh 'mvn site -T 4'
                }
            }

            post {
                always {
                    sh 'echo "skip"'
//                    jacoco exclusionPattern: '**/ClientServerChannelProtos*.class', skipCopyOfSrcFiles: true, sourceExclusionPattern: '**/*.*', sourceInclusionPattern: '', sourcePattern: 'x'
//                    recordIssues(sourceCodeEncoding: 'UTF-8', tools: [
//                            spotBugs(),
//                            checkStyle(),
//                            taskScanner(highTags: 'FIXME', normalTags: 'TODO', ignoreCase: true, includePattern: '**/*.java')
//                    ])
                }
            }
        }

        stage('Deploy') {
            when {
                expression { env.BRANCH_NAME == 'main' }
            }
            steps {
                container('maven-sumo') {
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