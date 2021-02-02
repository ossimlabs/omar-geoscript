// THIS IS A DRAFT FOR CONSISTENT JENKINSFILES

properties([
    parameters ([
        string(name: 'BUILD_NODE', defaultValue: 'POD_LABEL', description: 'The build node to run on'),
        booleanParam(name: 'CLEAN_WORKSPACE', defaultValue: true, description: 'Clean the workspace at the end of the run'),
        string(name: 'DOCKER_REGISTRY_DOWNLOAD_URL', defaultValue: 'nexus-docker-private-group.ossim.io', description: 'Repository of docker images')
    ]),
    pipelineTriggers([
            [$class: "GitHubPushTrigger"]
    ]),
    [$class: 'GithubProjectProperty', displayName: '', projectUrlStr: 'https://github.com/Maxar-Corp/omar-geoscript'],
    buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '20')),
    disableConcurrentBuilds()
])

podTemplate(
  containers: [
    containerTemplate(
      name: 'docker',
      image: 'docker:19.03.11',
      ttyEnabled: true,
      command: 'cat',
      privileged: true
    ),
    containerTemplate(
      image: "${DOCKER_REGISTRY_DOWNLOAD_URL}/omar-builder:jdk11",
      name: 'builder',
      command: 'cat',
      ttyEnabled: true
    ),
    containerTemplate(
      image: "${DOCKER_REGISTRY_DOWNLOAD_URL}/alpine/helm:3.2.3",
      name: 'helm',
      command: 'cat',
      ttyEnabled: true
    ),
    containerTemplate(
        name: 'git',
        image: 'alpine/git:latest',
        ttyEnabled: true,
        command: 'cat',
        envVars: [
            envVar(key: 'HOME', value: '/root')
        ]
    ),
    containerTemplate(
      image: "${DOCKER_REGISTRY_DOWNLOAD_URL}/kubectl-aws-helm:latest",
      name: 'kubectl-aws-helm',
      command: 'cat',
      ttyEnabled: true,
      alwaysPullImage: true
    ),
    containerTemplate(
        name: 'cypress',
        image: "${DOCKER_REGISTRY_DOWNLOAD_URL}/cypress/included:4.9.0",
        ttyEnabled: true,
        command: 'cat',
        privileged: true
    )
  ],
  volumes: [
    hostPathVolume(
      hostPath: '/var/run/docker.sock',
      mountPath: '/var/run/docker.sock'
    ),
  ]
)

{
  node(POD_LABEL){

      stage("Checkout branch")
      {
          scmVars = checkout(scm)

        APP_NAME = "omar-geoscript"
        MASTER = "master"
        DEV = "dev"
//         TimeZone.getTimeZone('UTC')
        Date date = new Date()
        String newDate = date.format("YYYY-MM-dd-HH-mm-ss")

        GIT_BRANCH_NAME = scmVars.GIT_BRANCH
        BRANCH_NAME = """${sh(returnStdout: true, script: "echo ${GIT_BRANCH_NAME} | awk -F'/' '{print \$2}'").trim()}"""
        VERSION = """${sh(returnStdout: true, script: "cat chart/Chart.yaml | grep version: | awk -F'version:' '{print \$2}'").trim()}"""
        GIT_TAG_NAME = APP_NAME + "-" + VERSION
        ARTIFACT_NAME = "ArtifactName"

        script {
          if (BRANCH_NAME == "${MASTER}") {
            buildName "${CHART_APP_VERSION}"
            TAG_NAME = CHART_APP_VERSION
          } else {
            buildName "${BRANCH_NAME}-${newDate}" // FIXME
            TAG_NAME = "${BRANCH_NAME}-${newDate}"
          }
        }
      }

      stage("Load Variables")
      {
        withCredentials([string(credentialsId: 'o2-artifact-project', variable: 'o2ArtifactProject')]) {
          step ([$class: "CopyArtifact",
            projectName: o2ArtifactProject,
            filter: "common-variables.groovy",
            flatten: true])
          }
          load "common-variables.groovy"

//         switch (BRANCH_NAME) {
//         case "${MASTER}":
//           TAG_NAME = VERSION
//           break
//
//         case "${DEV}":
//           TAG_NAME = "latest"
//           break
//
//         default:
//           TAG_NAME = BRANCH_NAME
//           break
//       }

    DOCKER_IMAGE_PATH = "${DOCKER_REGISTRY_PRIVATE_UPLOAD_URL}/${APP_NAME}"

    }

//     CYPRESS TESTS SHOULD BE COMING SOON
//     stage ("Run Cypress Test") {
//         container('cypress') {
//             try {
//                 sh """
//                 cypress run --headless
//                 """
//             } catch (err) {}
//             sh """
//                 npm i -g xunit-viewer
//                 xunit-viewer -r results -o results/APP_NAME-test-results.html
//                 """
//                 junit 'results/*.xml'
//                 archiveArtifacts "results/*.xml"
//                 archiveArtifacts "results/*.html"
//                 s3Upload(file:'results/${APP_NAME}-test-results.html', bucket:'ossimlabs', path:'cypressTests/')
//             }
//         }

//       stage('Fortify Scans') {
//       COMING SOON
//       }

      stage('SonarQube Analysis') {
          nodejs(nodeJSInstallationName: "${NODEJS_VERSION}") {
              def scannerHome = tool "${SONARQUBE_SCANNER_VERSION}"

              withSonarQubeEnv('sonarqube'){
                  sh """
                    ${scannerHome}/bin/sonar-scanner \
                    -Dsonar.projectKey=${APP_NAME} \
                    -Dsonar.login=${SONARQUBE_TOKEN}
                  """
              }
          }
      }

      stage('Build') {
        container('builder') {
          sh """
          ./gradlew assemble \
              -PossimMavenProxy=${MAVEN_DOWNLOAD_URL}
          ./gradlew copyJarToDockerDir \
              -PossimMavenProxy=${MAVEN_DOWNLOAD_URL}
          """
          archiveArtifacts "plugins/*/build/libs/*.jar"
          archiveArtifacts "apps/*/build/libs/*.jar"
        }
      }


    stage('Docker build') {
      container('docker') {
        withDockerRegistry(credentialsId: 'dockerCredentials', url: "https://${DOCKER_REGISTRY_DOWNLOAD_URL}") {
          sh """
            docker build --network=host -t "${DOCKER_REGISTRY_PUBLIC_UPLOAD_URL}/${APP_NAME}:${TAG_NAME}" ./docker
          """
        }
      }
    }

    stage('Docker push') {
      container('docker') {
        withDockerRegistry(credentialsId: 'dockerCredentials', url: "https://${DOCKER_REGISTRY_PRIVATE_UPLOAD_URL}") {
          sh """
            docker tag ${DOCKER_IMAGE_PATH}:${TAG_NAME} ${DOCKER_IMAGE_PATH}:${TAG_NAME}
            docker push ${DOCKER_IMAGE_PATH}:${TAG_NAME}
          """
        }
      }
    }

      stage('Package and Upload chart'){
          container('helm') {
            sh """
                mkdir packaged-chart
                helm package -d packaged-chart chart
              """

           withCredentials([usernameColonPassword(credentialsId: 'helmCredentials', variable: 'HELM_CREDENTIALS')]) {
             sh "curl -u ${HELM_CREDENTIALS} ${HELM_UPLOAD_URL} --upload-file packaged-chart/*.tgz -v"
          }
        }
      }

    stage('Tag Repo') {
      when (BRANCH_NAME == MASTER) {
        container('git') {
          withCredentials([sshUserPrivateKey(
          credentialsId: env.GIT_SSH_CREDENTIALS_ID,
          keyFileVariable: 'SSH_KEY_FILE',
          passphraseVariable: '',
          usernameVariable: 'SSH_USERNAME')]) {
            script {
                sh """
                  mkdir ~/.ssh
                  echo -e "StrictHostKeyChecking=no\nIdentityFile ${SSH_KEY_FILE}" >> ~/.ssh/config
                  git config user.email "radiantcibot@gmail.com"
                  git config user.name "Jenkins"
                  git tag -a "${GIT_TAG_NAME}" \
                    -m "Generated by: ${env.JENKINS_URL}" \
                    -m "Job: ${env.JOB_NAME}" \
                    -m "Build: ${env.BUILD_NUMBER}"
                  git push -v origin "${GIT_TAG_NAME}"
                """
            }
          }
        }
      }
    }
  }
}




