pipeline {
    agent {
        label 'kubeagent'
    }

    environment {
        KUBECONFIG = credentials('kubernetes-credentials')
    }

    stages {
        stage('Create Nginx Pod') {
            steps {
                script {
                    def nginxPodName = 'nginx-pod-name'
                    sh "kubectl run $nginxPodName --image=nginx --restart=Always"
                }
            }
        }

        stage('Download HTML and CSS') {
            steps {
                script {
                    def gitRepoUrl = 'https://github.com/Wijnen1/Jenkins-demo.git'
                    def gitRepoDir = 'github-repo'
                    def nginxPodName = 'nginx-pod-name'

                    sh "git clone $gitRepoUrl $gitRepoDir"
                    sh "kubectl cp $gitRepoDir/index.html $nginxPodName:/usr/share/nginx/html/index.html"
                    sh "kubectl cp $gitRepoDir/style.css $nginxPodName:/usr/share/nginx/html/style.css"
                }
            }
        }
    }

    post {
        success {
            echo 'De pod en bestanden zijn succesvol aangemaakt en bijgewerkt.'
        }
        failure {
            echo 'Het proces is mislukt.'
        }
    }
}
