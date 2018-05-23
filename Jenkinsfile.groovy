podTemplate(label: 'mypod', containers: [
        containerTemplate(name: 'docker', image: 'docker', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.0', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'curl', image: 'khinkali/jenkinstemplate:0.0.3', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'maven', image: 'maven:3.5.2-jdk-8', command: 'cat', ttyEnabled: true)
],
        volumes: [
                hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
        ]) {
    node('mypod') {
        properties([
                buildDiscarder(
                        logRotator(artifactDaysToKeepStr: '',
                                artifactNumToKeepStr: '',
                                daysToKeepStr: '',
                                numToKeepStr: '30'
                        )
                ),
                pipelineTriggers([cron('0 0 * * *')])
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