package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.MNIST.api.MNISTWebUI;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.MNIST.deployment.MNIST.deployMNIST;

@Loggable(project = "mnist")
public class Setup {
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private static MNISTWebUI MNIST;

  public MNISTWebUI initializeApplications() throws TimeoutException, InterruptedException {
    initializeProject();
    MNIST = deployMNIST();
    return MNIST;
  }

  public void cleanUp() {
    if(MNIST != null){
      MNIST.webDriverCleanup();
    }

    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }

}
