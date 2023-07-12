/*
apiVersion: v1
kind: ServiceAccount
metadata:
  name: jenkins-slave
  namespace: jenkins
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: jenkins-slave-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: jenkins-slave
  namespace: jenkins
*/


def call(Map propertyMap) {

    // If propertyMap is empty
    if (!propertyMap) {

        // Set to empty map
        propertyMap = []

    }

    // Set current directory
    propertyMap.currentDirectory = propertyMap.currentDirectory ? propertyMap.currentDirectory : "./"

    pipeline {

        agent {
            kubernetes {
                yaml """
                    apiVersion: "v1"
                    kind: "Pod"
                    spec:
                      securityContext:
                        runAsUser: 1001
                        runAsGroup: 1001
                        fsGroup: 1001
                      containers:
                      - command:
                        - "cat"
                        image: "maven:latest"
                        imagePullPolicy: "IfNotPresent"
                        name: "maven"
                        resources: {}
                        tty: true
                      serviceAccountName: jenkins-slave
                """
            }
        }

        stages {

            stage ("Build") {

                steps {

                    container ("maven") {
    
                        script {

                            dir(currentDirectory) {

                                // Build
                                sh "mvn clean install -DskipTests"

                            }

                        }

                    }

                }

            }

            stage ("Test") {

                steps {

                    container ("maven") {
    
                        script {

                            dir(currentDirectory) {

                                // Test
                                sh "mvn test"

                            }

                        }

                    }

                }

            }

        }

    }

}
