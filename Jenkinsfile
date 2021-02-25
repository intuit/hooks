def githubApi = 'https://github.intuit.com/api/v3'

@Library('fuego-libraries') _

pipeline {
  agent {
      kubernetes {
        // Use a dynamic pod name because static labels are known to cause pod creation errors.
        label "hooks-pod-${UUID.randomUUID().toString()}"
        defaultContainer "hooks"
        yaml """
        apiVersion: v1
        kind: Pod
        spec:
            containers:
            - name: hooks
              image: 'app/utilities/android-player/service/jvm:latest'
              resources:
                requests:
                  memory: 4G
              command:
              - cat
              tty: true
        """
      }
  }
  options {
    timestamps()
    buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '50', artifactNumToKeepStr: '30'))
  }
  environment {
    // Auto Tooling
    REPO = 'hooks'
    OWNER = 'player'
    GH_TOKEN = credentials('NATIVE_APPS_GH_TOKEN')
    PRIVATE_TOKEN = credentials('NATIVE_APPS_GH_TOKEN')
    SLACK_TOKEN = credentials('TAPABLE_KT_SLACK_TOKEN')

    // Doc publishing
    GITHUB_CREDENTIALS = credentials("97506aff-6000-4b86-a3f3-04ec9de71845")
    GITHUB_USER = "${env.GITHUB_CREDENTIALS_USR}"
    GITHUB_TOKEN = "${env.GITHUB_CREDENTIALS_PSW}"

    // Library publishing
    ARTIFACTORY_CREDENTIALS = credentials('ibp-nexus-creds')
    ARTIFACTORY_USERNAME = "${env.ARTIFACTORY_CREDENTIALS_USR}"
    ARTIFACTORY_PASSWORD = "${env.ARTIFACTORY_CREDENTIALS_PSW}"
  }
  stages {
    stage('Check Skip CI') {
      steps {
        script {
          // Set description as the current version
          currentBuild.description = sh(script: "gradle -q version", returnStdout: true).trim()

          result = sh (script: "git log -1 | grep '.*\\[skip ci\\].*'", returnStatus: true)
          if (result == 0) {
              scmSkip(deleteBuild: true)
              echo ("'Skip CI' spotted in git commit. Aborting.")
              currentBuild.result = 'ABORTED'
              error('Exiting job');
          }
        }
      }
    }
    stage('Auth') {
      steps {
        sh """#!/bin/bash -l
          echo "https://${GITHUB_USER}:${GITHUB_TOKEN}@github.intuit.com" >> /tmp/gitcredfile
          git config --global user.name "${GITHUB_USER}"
          git config --global user.email "Jeremiah_Zucker@intuit.com"
          git config --global credential.helper "store --file=/tmp/gitcredfile"
          git config --global merge.renameLimit 999999
          git config --global diff.renameLimit 999999
        """
      }
    }
    stage('🏗 Build') {
      steps {
          sh 'gradle build'
      }
    }
    stage('Test') {
      failFast true
      parallel {
        stage('PR Version Check') {
          when { changeRequest() }
          steps {
            sh 'auto pr-check --pr $CHANGE_ID --url $BUILD_URL'
          }
        }
        stage('✅ Unit Testing') {
          steps {
            sh 'gradle test'
          }
        }
      }
      post {
       always {
         junit '**/build/test-results/**/*.xml'
         jacoco(
           execPattern: '**/build/jacoco/*.exec',
           classPattern: '**/build/classes',
           sourcePattern: '**/src/main*',
           exclusionPattern: '**/src/test*'
         )
       }
     }
    }
    stage('Artifactory Release') {
      parallel {
        stage('Snapshot') {
          when { changeRequest() }
          steps {
            sh 'git checkout -B tmp'
            sh 'gradle publish'
          }
        }
        stage('Latest') {
          when { branch 'master' }
          steps {
            sh 'git checkout master'
            sh 'auto shipit'
          }
        }
      }
    }
    stage('📄 Generate docs') {
      steps {
        sh 'gradle orchidDeploy'
      }
    }
  }
}
