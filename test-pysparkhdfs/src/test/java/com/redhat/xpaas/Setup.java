package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.PySparkHDFS.api.PySparkHDFSWebUI;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.PySparkHDFS.deployment.PySparkHDFS.deployPySparkHDFS;

@Loggable(project ="pysparkhdfs")
public class Setup {
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private static PySparkHDFSWebUI pySparkHDFS;

  public PySparkHDFSWebUI initializeApplications() throws TimeoutException, InterruptedException {
    initializeProject();
    Oshinko.deploySparkFromResource();
    pySparkHDFS = deployPySparkHDFS();
    return pySparkHDFS;
  }

  public void cleanUp() {
    if(pySparkHDFS != null){
      pySparkHDFS.webDriverCleanup();
    }

    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }

}
