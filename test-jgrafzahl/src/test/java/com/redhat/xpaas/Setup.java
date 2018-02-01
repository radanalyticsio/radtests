package com.redhat.xpaas;

import com.redhat.xpaas.logger.LogWrapper;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.apachekafka.deployment.ApacheKafka;
import com.redhat.xpaas.rad.jgrafzahl.api.JgrafZahlWebUI;
import com.redhat.xpaas.rad.wordfountain.deployment.WordFountain;
import com.redhat.xpaas.wait.WaitUtil;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.jgrafzahl.deployment.JgrafZahl.deployJgrafZahl;
import static com.redhat.xpaas.rad.jgrafzahl.deployment.GrafZahl.deployGrafZahl;

public class Setup {
  LogWrapper log = new LogWrapper(Setup.class, "grafzahl");
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static JgrafZahlWebUI JgrafZahl;
  private static JgrafZahlWebUI GrafZahl;

  // Returns an array of size 2 where at index 0 contains Jgrafzahl web UI and index 2
  // contains Grafzahl's web ui api.
  JgrafZahlWebUI[] initializeApplications() {
    log.action("creating-new-namespace", this::initializeProject);

    log.action("loading-oshinko-resources", () -> {
      Oshinko.createServiceAccount("edit");
      Oshinko.loadJavaSparkResources();
    });

    log.action("launching-apache-kafka", ApacheKafka::deployApacheKafka);
    log.action("launching-wordfountain", WordFountain::deployWordFountain);
    log.action("launching-jgrafzahl", () -> JgrafZahl = deployJgrafZahl());
    log.action("launching-grafzahl", () -> GrafZahl = deployGrafZahl());

    log.action("waiting-for-pods-to-ready", () -> {
      WaitUtil.waitForActiveBuildsToComplete();
      try {
        WaitUtil.waitFor(WaitUtil.isAPodReady("word-fountain"));
        WaitUtil.waitFor(WaitUtil.isAPodReady("jgrafzahl"));
        WaitUtil.waitFor(WaitUtil.isAPodReady("grafzahl"));
        WaitUtil.waitFor(WaitUtil.areNWorkerReady(2));
        WaitUtil.waitFor(WaitUtil.areNMastersReady(2));
      } catch (InterruptedException | TimeoutException e) {
        e.printStackTrace();
      }
    });

    return new JgrafZahlWebUI[]{JgrafZahl, GrafZahl};
  }

  void cleanUp() {
    if(JgrafZahl != null){
      log.action("shutting-down-jgrafzahl-webdrivers", () -> JgrafZahl.webDriverCleanup());
    }

    if(GrafZahl != null){
      log.action("shutting-down-grafzahl-webdrivers", () -> GrafZahl.webDriverCleanup());
    }

    log.action("deleting-namespace", () -> openshift.deleteProject(NAMESPACE));
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, true);
  }

}
