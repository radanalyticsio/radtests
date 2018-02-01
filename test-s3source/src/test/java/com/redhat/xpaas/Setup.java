package com.redhat.xpaas;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.S3Source.api.S3SourceWebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.xpaas.rad.S3Source.deployment.S3Source.deployS3Source;

public class Setup {
  private Logger log = LoggerFactory.getLogger(Setup.class);
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static S3SourceWebUI S3Source;

  S3SourceWebUI initializeApplications() {
    Logger log = LoggerFactory.getLogger(WebUITest.class);
    log.info("project=s3source action=creating-new-namespace status=START");
    initializeProject();
    log.info("project=s3source action=creating-new-namespace status=FINISH");
    log.info("project=s3source action=deploy-s3source status=START");
    S3Source = deployS3Source();
    log.info("project=s3source action=deploy-s3source status=FINISH");
    return S3Source;
  }

  void cleanUp() {
    if(S3Source != null){
      log.info("project=s3source action=shutting-down-webdrivers status=START");
      S3Source.webDriverCleanup();
      log.info("project=s3source action=shutting-down-webdrivers status=FINISH");
    }
    log.info("project=s3source action=deleting-namespace status=START");
    openshift.deleteProject(NAMESPACE);
    log.info("project=s3source action=shutting-down-webdrivers status=FINISH");
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, true);
  }


}
