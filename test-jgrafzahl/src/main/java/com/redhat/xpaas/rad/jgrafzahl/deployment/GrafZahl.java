package com.redhat.xpaas.rad.jgrafzahl.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.jgrafzahl.api.JgrafZahlWebUI;
import com.redhat.xpaas.wait.WaitUtil;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.oshinko.deployment.Oshinko.deployPySparkSpark;

public class GrafZahl {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final String ROUTE = "/grafzahl/route.yaml";

  public static JgrafZahlWebUI deployGrafZahl() throws TimeoutException, InterruptedException {
    deployPySparkSpark(
      "grafzahl",
      "https://github.com/radanalyticsio/grafzahl",
      "--servers=apache-kafka:9092",
      "--packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.1.0");

    // Expose route
    openshift.withAdminUser(client ->
      client.routes().inNamespace(NAMESPACE).load(JgrafZahl.class.getResourceAsStream(ROUTE)).createOrReplace()
    );

    // Expose route
    boolean succeeded = WaitUtil.waitForActiveBuildsToComplete();

    if(!succeeded){
      throw new IllegalStateException(LoggerUtil.openshiftError("grafzahl builds", "build"));
    }

    succeeded = WaitUtil.waitFor(WaitUtil.isAPodReady("grafzahl"));

    if(!succeeded){
      throw new IllegalStateException(LoggerUtil.openshiftError("grafzahl deployment", "pod"));
    }

    return JgrafZahlWebUI.getInstance(openshift.appDefaultHostNameBuilder("grafzahl"));
  }

}
