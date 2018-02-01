package com.redhat.xpaas;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.ValueAtRisk.api.ValueAtRiskWebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.xpaas.rad.ValueAtRisk.deployment.ValueAtRisk.deployValueAtRisk;

public class Setup {
  private Logger log = LoggerFactory.getLogger(Setup.class);
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  private ValueAtRiskWebUI ValueAtRisk;

  ValueAtRiskWebUI initializeApplications() {
    Logger log = LoggerFactory.getLogger(WebUITest.class);
    log.info("action=creating-new-namespace status=START");
    initializeProject();
    log.info("action=creating-new-namespace status=FINISH");

    log.info("action=deploy-ValueAtRisk status=START");
    ValueAtRisk = deployValueAtRisk();
    log.info("action=deploy-ValueAtRisk status=FINISH");

    return ValueAtRisk;
  }

  void cleanUp() {
    if(ValueAtRisk != null){
      log.info("action=shutting-down-webdrivers status=START");
      ValueAtRisk.webDriverCleanup();
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
