package com.redhat.xpaas;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.CephSource.api.CephSourceWebUI;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.CephSource.deployment.CephSource.deployCephSource;

public class Setup {
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static CephSourceWebUI CephSource;

  public CephSourceWebUI initializeApplications() throws TimeoutException, InterruptedException {
    initializeProject();
    Oshinko.deploySparkFromResource();
    CephSource = deployCephSource();
    return CephSource;
  }

  public void cleanUp() {
    if(CephSource != null){
      CephSource.webDriverCleanup();
    }

    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }

}
