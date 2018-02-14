package com.redhat.xpaas.rad.jgrafzahl.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.jgrafzahl.api.JgrafZahlWebUI;
import com.redhat.xpaas.wait.WaitUtil;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.oshinko.deployment.Oshinko.deployJavaSpark;

public class JgrafZahl {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final String ROUTE = "/jgrafzahl/route.yaml";

  public static JgrafZahlWebUI deployJgrafZahl() throws TimeoutException, InterruptedException {
    deployJavaSpark(
      "jgrafzahl",
      "https://github.com/radanalyticsio/jgrafzahl",
      "io.radanalytics.jgrafzahl.App",
      "apache-kafka:9092 word-fountain",
      "--packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.1.0,com.sparkjava:spark-core:2.5.5,org.glassfish:javax.json:1.0.4  --conf spark.jars.ivy=/tmp/.ivy2");

    // Expose route
    openshift.withAdminUser(client ->
      client.routes().inNamespace(NAMESPACE).load(JgrafZahl.class.getResourceAsStream(ROUTE)).createOrReplace()
    );

    boolean succeeded = WaitUtil.waitForActiveBuildsToComplete();
    if(!succeeded){
      throw new IllegalStateException(LoggerUtil.openshiftError("jgrafzahl builds", "build"));
    }

    succeeded = WaitUtil.waitFor(WaitUtil.isAPodReady("jgrafzahl"));
    if(!succeeded){
      throw new IllegalStateException(LoggerUtil.openshiftError("jgrafzahl deployment", "pod"));
    }

    return JgrafZahlWebUI.getInstance(openshift.appDefaultHostNameBuilder("jgrafzahl"));
  }

}
