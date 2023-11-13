pipeline {
    agent any

    environment {
        KUBECONFIG = credentials('kubernetes-credentials')
    }

    stages {
        stage('Create Nginx Deployment') {
            steps {
                script {
                    def nginxDeploymentName = 'nginx-deployment'
                    def nginxContainerName = 'nginx-container'
                    def gitRepoUrl = 'https://github.com/Wijnen1/Jenkins-demo.git'
                    def gitRepoDir = 'github-repo'

                    sh "git clone $gitRepoUrl $gitRepoDir"
                    sh "kubectl create deployment $nginxDeploymentName --image=nginx --dry-run=client -o yaml > deployment.yaml"
                    sh "kubectl apply -f deployment.yaml"

                    // Wait for the deployment to be ready before proceeding
                    sh "kubectl rollout status deployment/$nginxDeploymentName"

                    // Copy files to the pod
                    sh "kubectl cp $gitRepoDir/index.html $(kubectl get pod -l app=$nginxDeploymentName -o jsonpath='{.items[0].metadata.name}'):/usr/share/nginx/html/index.html"
                    sh "kubectl cp $gitRepoDir/style.css $(kubectl get pod -l app=$nginxDeploymentName -o jsonpath='{.items[0].metadata.name}'):/usr/share/nginx/html/style.css"
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
