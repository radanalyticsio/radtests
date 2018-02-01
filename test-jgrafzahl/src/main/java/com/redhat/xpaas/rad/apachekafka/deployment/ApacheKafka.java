package com.redhat.xpaas.rad.apachekafka.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class ApacheKafka {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final String KAFKA_RESOURCES = "/apache-kafka/apache-kafka-resources.yaml";
  private static final String APP_NAME = "apache-kafka";

  public static void deployApacheKafka(){
    openshift.withAdminUser(client ->
      client.inNamespace(NAMESPACE).load(ApacheKafka.class.getResourceAsStream(KAFKA_RESOURCES))
        .deletingExisting()
        .createOrReplace()
    );

    try {
      WaitUtil.waitFor(_areNClusterPodsReady(APP_NAME, 1));
    } catch (InterruptedException | TimeoutException e) {
      throw new IllegalStateException("Timeout expired while waiting for Oshinko server availability");
    }
  }

  private static BooleanSupplier _areNClusterPodsReady(String name, int n){
    Predicate<Pod> podFilter = pod -> pod.getMetadata().getName().startsWith(name) && isKafkaPodReady(pod);
    return () -> openshift.getPods().stream().filter(podFilter).count() == n;
  }

  public static boolean isKafkaPodReady(Pod pod) {
    return "Running".equals(pod.getStatus().getPhase()) && !pod.getMetadata().getName().contains("deploy");
  }
}
