pipeline {
    agent {
        label 'kubeagent'
    }

    environment {
        KUBECONFIG = credentials('kubernetes-credentials')
    }

    stages {
        stage('Create Jenkins Agent Pod') {
            steps {
                script {
                    def jnlpPodName = 'jnlp-pod-name'
                    def jnlpImage = 'jenkins/jnlp-slave:latest'
                    
                    // Create Jenkins Agent (JNLP) pod
                    sh "kubectl run $jnlpPodName --image=$jnlpImage --restart=Always"
                }
            }
        }

        stage('Create Nginx Pod') {
            steps {
                script {
                    def nginxPodName = 'nginx-pod-name'
                    
                    // Create NGINX pod
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
                    def jnlpPodName = 'jnlp-pod-name'

                    // Clone the Git repository
                    sh "git clone $gitRepoUrl $gitRepoDir"

                    // Copy HTML and CSS files to NGINX pod
                    sh "kubectl cp $gitRepoDir/index.html $nginxPodName:/usr/share/nginx/html/index.html"
                    sh "kubectl cp $gitRepoDir/style.css $nginxPodName:/usr/share/nginx/html/style.css"

                    // Copy Git repository to Jenkins Agent (JNLP) pod
                    sh "kubectl cp $gitRepoDir $jnlpPodName:/workspace/"
                }
            }
        }
    }

    post {
        success {
            echo 'De pods en bestanden zijn succesvol aangemaakt en bijgewerkt.'
        }
        failure {
            echo 'Het proces is mislukt.'
        }
    }
}
