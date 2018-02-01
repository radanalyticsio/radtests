package com.redhat.xpaas;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.MNIST.api.MNISTWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.xpaas.rad.MNIST.deployment.MNIST.deployMNIST;

public class Setup {
  private Logger log = LoggerFactory.getLogger(Setup.class);
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  private static MNISTWebUI MNIST;

  MNISTWebUI initializeApplications() {
    Logger log = LoggerFactory.getLogger(WebUITest.class);
    log.info("action=creating-new-namespace status=START");
    initializeProject();
    log.info("action=creating-new-namespace status=FINISH");

    log.info("action=deploy-MNIST status=START");
    MNIST = deployMNIST();
    log.info("action=deploy-MNIST status=FINISH");

    return MNIST;
  }

  void cleanUp() {
    if(MNIST != null){
      log.info("action=shutting-down-webdrivers status=START");
      MNIST.webDriverCleanup();
      log.info("action=shutting-down-webdrivers status=FINISH");
    }

    log.info("action=deleting-namespace status=START");
    openshift.deleteProject(NAMESPACE);
    log.info("action=shutting-down-webdrivers status=FINISH");
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, true);
  }

}
