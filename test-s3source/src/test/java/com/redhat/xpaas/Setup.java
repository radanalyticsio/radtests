package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.S3Source.api.S3SourceWebUI;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.S3Source.deployment.S3Source.deployS3Source;

@Loggable(project = "S3Source")
public class Setup {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private static S3SourceWebUI S3Source;

  public S3SourceWebUI initializeApplications() throws TimeoutException, InterruptedException {
    initializeProject();
    S3Source = deployS3Source();
    return S3Source;
  }

  public void cleanUp() {
    if(S3Source != null){
      S3Source.webDriverCleanup();
    }

    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }

}
