podTemplate(label: 'mypod') {
    node('mypod') {
        withCredentials([sshUserPrivateKey(credentialsId: 'server', keyFileVariable: 'keyfile', usernameVariable: 'username')]) {
            withCredentials([usernamePassword(credentialsId: 'bitbucket', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                sh "mkdir ~/.ssh/"
                def hosts = ['18.195.197.32', '18.196.37.97', '18.195.180.75', '18.196.67.191']
                for(String host : hosts) {
                    pullRepo(host)
                    commitAndPushRepo(host)
                }
            }    
        }
    }
}

def pullRepo(String host) {
    sh "ssh-keyscan -t rsa ${host} >> ~/.ssh/known_hosts"
    sh "ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' pull https://${GIT_USERNAME}:${GIT_PASSWORD}@bitbucket.org/khinkali/jenkins_backup"
}

def commitAndPushRepo(String host) {
    sh "ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' add --all"
    sh "ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' commit -m 'new version'"
    sh "ssh -i ${keyfile} ${username}@${host} git -C '/home/${username}/jenkins_backup' push https://${GIT_USERNAME}:${GIT_PASSWORD}@bitbucket.org/khinkali/jenkins_backup"
}
