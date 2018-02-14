package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.mongodb.deployment.MongoDB;
import com.redhat.xpaas.rad.ophicleide.api.OphicleideWebUI;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.ophicleide.deployment.Ophicleide;

import java.util.concurrent.TimeoutException;

@Loggable(project ="ophicleide")
public class Setup {

  private String NAMESPACE = RadConfiguration.masterNamespace();
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static OphicleideWebUI ophicleide;

  public OphicleideWebUI initializeApplications() throws TimeoutException, InterruptedException {
    initializeProject();
    Oshinko.deploySparkFromResource();
    MongoDB.deployMongoDBPod();
    ophicleide = Ophicleide.deployOphicleideWebUI();
    return ophicleide;
  }

  public void cleanUp() {
    if(ophicleide != null){
      ophicleide.webDriverCleanup();
    }
    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }

}
