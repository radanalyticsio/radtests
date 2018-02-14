package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.ValueAtRisk.api.ValueAtRiskWebUI;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.ValueAtRisk.deployment.ValueAtRisk.deployValueAtRisk;

@Loggable(project = "valueatrisk")
public class Setup {

  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private ValueAtRiskWebUI ValueAtRisk;

  public ValueAtRiskWebUI initializeApplications() throws TimeoutException, InterruptedException {
    initializeProject();
    ValueAtRisk = deployValueAtRisk();
    return ValueAtRisk;
  }

  public void cleanUp() {
    if(ValueAtRisk != null){
      ValueAtRisk.webDriverCleanup();
    }

    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }

}
