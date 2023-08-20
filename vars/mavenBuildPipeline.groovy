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
                        image: "maven:3-amazoncorretto"
                        imagePullPolicy: "IfNotPresent"
                        name: "maven"
                        resources: {}
                        tty: true
                        volumeMounts:
                        - mountPath: "/.m2/repository"
                          name: "m2-volume"
                          readOnly: false
                        - mountPath: "/.npm"
                          name: "npm-volume"
                          readOnly: false
                        - mountPath: "/.embedmongo"
                          name: "embedmongo"
                          readOnly: false
                      serviceAccountName: jenkins-slave
                      volumes:
                      - emptyDir:
                          medium: ""
                        name: "m2-volume"
                      - emptyDir:
                          medium: ""
                        name: "npm-volume"
                      - emptyDir:
                          medium: ""
                        name: "embedmongo"
                """
            }
        }

        stages {

            stage ("Build") {

                steps {

                    container ("maven") {
    
                        script {

                            dir(propertyMap.currentDirectory) {

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

                            dir(propertyMap.currentDirectory) {

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
