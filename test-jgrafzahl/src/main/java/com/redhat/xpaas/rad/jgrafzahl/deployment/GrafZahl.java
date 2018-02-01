package com.redhat.xpaas.rad.jgrafzahl.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.jgrafzahl.api.JgrafZahlWebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.xpaas.oshinko.deployment.Oshinko.deployPySparkSpark;

public class GrafZahl {
  private static final Logger log = LoggerFactory.getLogger(JgrafZahl.class);
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final String ROUTE = "/grafzahl/route.yaml";

  public static JgrafZahlWebUI deployGrafZahl() {
    deployPySparkSpark(
      "grafzahl",
      "https://github.com/radanalyticsio/grafzahl",
      "--servers=apache-kafka:9092",
      "--packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.1.0");

    // Expose route
    openshift.withAdminUser(client ->
      client.routes().inNamespace(NAMESPACE).load(JgrafZahl.class.getResourceAsStream(ROUTE)).createOrReplace()
    );

    return JgrafZahlWebUI.getInstance(openshift.appDefaultHostNameBuilder("grafzahl"));
  }
}
