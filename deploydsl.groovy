String basePath = 'jobfolder'
String gitRepository = 'amenaafreen/kubernetesdeployment'

folder(basePath) {
    description('Folder containing all jobs for folder')
}
   mavenJob("$basePath/maven-job") {
   description('Build the Java Project: ' + gitRepository)
    scm {
        github(gitRepository, 'master')
    }
   triggers {
     githubPush()
    }

   goals('clean package')
   publishers {
       //archive the war file generated
       archiveArtifacts 'target/*.war'
}
     postBuildSteps{

     shell ("""kubectl apply -f webdeploy.yml &&\
                kubectl apply -f mysqldeployment.yml &&\
               sleep 10s
               echo "APP URL" &&\
               curl -Is http://localhost:30003/LoginWebApp/""")
       
     }
     
     postBuildSteps{
        configure{ project ->
    project/publishers << 'org.jfrog.hudson.ArtifactoryRedeployPublisher' {
    details {
      artifactoryUrl('http://localhost:8081/artifactory')
      artifactoryName('Artifactory Version 4.15.0')
      repositoryKey('Jenkins-Integration')
      snapshotsRepositoryKey('Jenkins-Snapshot')
    }
    deployBuildInfo(true)
    deployArtifacts(true)
    evenIfUnstable(false)
      }
   }
}
}