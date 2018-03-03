podTemplate(label: 'mypod') {
    node('mypod') {
        properties([
                buildDiscarder(
                        logRotator(artifactDaysToKeepStr: '',
                                artifactNumToKeepStr: '',
                                daysToKeepStr: '',
                                numToKeepStr: '30'
                        )
                ),
                pipelineTriggers([cron('1 0 * * *')])
        ])

        stage('create backup') {
            def kc = 'kubectl'
            container('kubectl') {
                def jenkinsPods = sh(
                        script: "${kc} get po -l app=jenkins --no-headers",
                        returnStdout: true
                ).trim()
                def podNameLine = jenkinsPods.split('\n')[0]
                def startIndex = podNameLine.indexOf(' ')
                if (startIndex == -1) {
                    return
                }
                def podName = podNameLine.substring(0, startIndex)
                sh "${kc} exec ${podName} -- git -C '/var/jenkins_home' config user.email \"jenkins@khinkali.ch\""
                sh "${kc} exec ${podName} -- git -C '/var/jenkins_home' config user.name \"Jenkins\""
                sh "${kc} exec ${podName} -- git -C '/var/jenkins_home' add --all"
                sh "${kc} exec ${podName} -- git -C '/var/jenkins_home' diff --quiet && ${kc} exec ${podName} -- git -C '/var/jenkins_home' diff --staged --quiet || ${kc} exec ${podName} -- git -C '/var/jenkins_home' commit -am 'new_version'"
                withCredentials([usernamePassword(credentialsId: 'bitbucket', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh "${kc} exec ${podName} -- git -C '/var/jenkins_home' push https://${GIT_USERNAME}:${GIT_PASSWORD}@bitbucket.org/khinkali/jenkins_backup"
                }
            }
        }

    }
}