

pipeline {

  environment {
    JAVA_HOME="${tool 'Open JDK-11u28'}"
    PATH="${env.JAVA_HOME}/bin:${env.PATH}"
  }

  agent{ label 'linux && !fast-aws-deploy'}

  triggers {
    pollSCM 'H/5 * * * *'
  }


  options {
    disableConcurrentBuilds()
    buildDiscarder(logRotator(daysToKeepStr: '5', numToKeepStr: '5'))
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }


    stage('build and test') {
      steps {
        script {
          try {
              sh "chmod +x gradlew"
              sh "./gradlew clean build"
          } catch(err) {
              throw err
          } finally {
              junit '**/build/test-results/TESTS-*.xml'
          }
        }
      }
    }


    stage ('quality analysis') {
      steps {
        // the tests are already run in the previous steps
        sh "./gradlew sonarqube"
      }
    }

    stage ('package and release') {
      steps {
        sh "./gradlew shadowJar publish"
      }
    }

  }


}
