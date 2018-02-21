package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.winemap.api.WinemapWebUI;
import com.redhat.xpaas.rad.winemap.deployment.Winemap;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.postgresql.PostgreSQL.deployPostgreSQL;

@Loggable(project = "winemap")
public class Setup {
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static WinemapWebUI winemap;

  public WinemapWebUI initializeApplications() throws TimeoutException, InterruptedException {
    initializeProject();
    deployPostgreSQL();
    winemap = Winemap.deployWinemapWebUI();
    return winemap;
  }

  public void cleanUp() {
    if(winemap != null){
      winemap.webDriverCleanup();
    }
    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }
}
