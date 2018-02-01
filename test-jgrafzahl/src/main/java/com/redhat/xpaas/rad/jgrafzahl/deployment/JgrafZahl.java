package com.redhat.xpaas.rad.jgrafzahl.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.jgrafzahl.api.JgrafZahlWebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.xpaas.oshinko.deployment.Oshinko.deployJavaSpark;

public class JgrafZahl {
  private static final Logger log = LoggerFactory.getLogger(JgrafZahl.class);
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final Long TIMEOUT = RadConfiguration.timeout();
  private static final String ROUTE = "/jgrafzahl/route.yaml";

  public static JgrafZahlWebUI deployJgrafZahl() {
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

    return JgrafZahlWebUI.getInstance(openshift.appDefaultHostNameBuilder("jgrafzahl"));

  }

}
