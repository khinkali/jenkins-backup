podTemplate(label: 'mypod') {
    node('mypod') {
        properties([
                buildDiscarder(
                        logRotator(artifactDaysToKeepStr: '',
                                artifactNumToKeepStr: '',
                                daysToKeepStr: '',
                                numToKeepStr: '30'
                        )
                )
        ])
        triggers {
            cron('1 0 * * *')
        }

        stage('create backup') {
            withCredentials([sshUserPrivateKey(credentialsId: 'server', keyFileVariable: 'keyfile', usernameVariable: 'username')]) {
                withCredentials([usernamePassword(credentialsId: 'bitbucket', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh "mkdir ~/.ssh/"
                    def hosts = ['18.195.197.32', '18.196.37.97', '18.195.180.75', '18.196.67.191']
                    for(String host : hosts) {
                        addToKnownHosts(host)
                        pullRepo(host)
                        commitAndPushRepo(host)
                    }
                    for(String host : hosts) {
                        pullRepo(host)
                    }
                }
            }
        }
    }
}

def addToKnownHosts(String host) {
    sh "ssh-keyscan -t rsa ${host} >> ~/.ssh/known_hosts"
}

def pullRepo(String host) {
    sh "ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' pull https://${GIT_USERNAME}:${GIT_PASSWORD}@bitbucket.org/khinkali/jenkins_backup"
}

def commitAndPushRepo(String host) {
    sh "ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' add --all"
    sh "ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' diff --quiet && ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' diff --staged --quiet || ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' commit -am 'new_version'"
    sh "ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' push https://${GIT_USERNAME}:${GIT_PASSWORD}@bitbucket.org/khinkali/jenkins_backup"
}
