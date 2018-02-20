package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.apachekafka.deployment.ApacheKafka;
import com.redhat.xpaas.rad.jgrafzahl.api.JgrafZahlWebUI;
import com.redhat.xpaas.rad.wordfountain.deployment.WordFountain;
import com.redhat.xpaas.wait.WaitUtil;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.jgrafzahl.deployment.JgrafZahl.deployJgrafZahl;
import static com.redhat.xpaas.rad.jgrafzahl.deployment.GrafZahl.deployGrafZahl;

@Loggable(project ="grafzahl")
public class Setup {
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static JgrafZahlWebUI JgrafZahl;
  private static JgrafZahlWebUI GrafZahl;

  // Returns an array of size 2 where at index 0 contains Jgrafzahl web UI and index 2
  // contains Grafzahl's web ui api.
  public JgrafZahlWebUI[] initializeApplications() throws TimeoutException, InterruptedException {

    initializeProject();

    Oshinko.createServiceAccount("edit");
    Oshinko.loadJavaSparkResources();

    ApacheKafka.deployApacheKafka();
    WordFountain.deployWordFountain();

    // The following will also deploy the spark master/workers
    JgrafZahl = deployJgrafZahl();
    GrafZahl = deployGrafZahl();

    // In order to account for all worker/masters for both grafzahls we wait for their deployments here
    if(!WaitUtil.waitFor(WaitUtil.areNWorkerReady(2))){
      throw new IllegalStateException(LoggerUtil.openshiftError("spark-worker deployment", "pod"));
    }

    if(!WaitUtil.waitFor(WaitUtil.areNMastersReady(2))){
      throw new IllegalStateException(LoggerUtil.openshiftError("spark-master deployment", "pod"));
    }

    return new JgrafZahlWebUI[]{JgrafZahl, GrafZahl};
  }

  public void cleanUp() {
    if(JgrafZahl != null){
      JgrafZahl.webDriverCleanup();
    }

    if(GrafZahl != null){
      GrafZahl.webDriverCleanup();
    }

    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }

}
