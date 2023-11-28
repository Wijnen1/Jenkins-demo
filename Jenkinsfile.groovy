pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    kubeagent: agent
spec:
  containers:
  - name: jnlp
    image: 'jenkins/jnlp-slave:latest'
  - name: nginx-container
    image: nginx:latest
    volumeMounts:
    - name: nginx-config
      mountPath: /etc/nginx/conf.d
  volumes:
  - name: nginx-config
    hostPath:
      path: ${WORKSPACE}/etc/nginx
"""
        }
    }

    environment {
        KUBECONFIG = credentials('kubernetes-credentials')
    }

    stages {
        stage('Clone Git Repository') {
            steps {
                script {
                    // Clean workspace before cloning
                    deleteDir()

                    // Clone the Git repository
                    git branch: 'main', credentialsId: '7b908686-b320-4d0b-b18a-48804e42b46f', url: 'https://github.com/Wijnen1/Jenkins-demo.git'
                }
            }
        }

        stage('Deploy NGINX Pod') {
            steps {
                script {
                    // Deployment steps remain the same
                    def nginxPod = """apiVersion: v1
kind: Pod
metadata:
  name: nginx-pod
spec:
  containers:
  - name: nginx-container
    image: nginx:latest
    volumeMounts:
    - name: nginx-config
      mountPath: /etc/nginx/conf.d
  volumes:
  - name: nginx-config
    hostPath:
      path: ${WORKSPACE}/etc/nginx
"""
                    writeFile file: 'nginx-pod.yaml', text: nginxPod

                    sh 'kubectl apply -f nginx-pod.yaml'
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    // Add verification steps if needed
                    sh 'kubectl get pods'
                }
            }
        }
    }

post {
    always {
        node {
            // Clean up resources
            script {
                sh 'kubectl delete pod nginx-pod'
            }
        }
    }
}
