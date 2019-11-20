timeout(time: 90, unit: 'MINUTES') {
    dir ("tests/milvus_python_test") {
        sh 'python3 -m pip install -r requirements.txt'
        sh "pytest . --alluredir=\"test_out/dev/single/sqlite\" --ip ${env.PIPELINE_NAME}-${env.BUILD_NUMBER}-single-${env.BINRARY_VERSION}-milvus-engine.milvus.svc.cluster.local"
    }
    // mysql database backend test
    load "${env.WORKSPACE}/ci/jenkins/jenkinsfile/cleanupSingleDev.groovy"

    if (!fileExists('milvus-helm')) {
        dir ("milvus-helm") {
            checkout([$class: 'GitSCM', branches: [[name: "0.6.0"]], userRemoteConfigs: [[url: "https://github.com/milvus-io/milvus-helm.git", name: 'origin', refspec: "+refs/heads/0.6.0:refs/remotes/origin/0.6.0"]]])
        }
    }
    dir ("milvus-helm") {
        dir ("milvus") {
            sh "helm install --wait --timeout 300 --set engine.image.tag=${DOCKER_VERSION} --set expose.type=clusterIP --name ${env.PIPELINE_NAME}-${env.BUILD_NUMBER}-single-${env.BINRARY_VERSION} -f ci/db_backend/mysql_${env.BINRARY_VERSION}_values.yaml -f ci/filebeat/values.yaml --namespace milvus ."
        }
    }
    dir ("tests/milvus_python_test") {
        sh "pytest . --alluredir=\"test_out/dev/single/mysql\" --ip ${env.PIPELINE_NAME}-${env.BUILD_NUMBER}-single-${env.BINRARY_VERSION}-milvus-engine.milvus.svc.cluster.local"
    }
}