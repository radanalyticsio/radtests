package com.redhat.xpaas.rad.ophicleide.deployment;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.ophicleide.api.OphicleideWebUI;
import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.BuildRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class Ophicleide {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String APP_NAME = RadConfiguration.ophicleideAppName();
  private static final int WORKERS_COUNT = RadConfiguration.oshinkoInitialWorkerCount();
  private static final String CLUSTER_NAME = RadConfiguration.clusterName();
  private static final Long TIMEOUT = RadConfiguration.timeout();
  private static final String MONGODB_APP_NAME = RadConfiguration.mongodbAppName();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static OphicleideWebUI deployOphicleideWebUI() {
    initializeOphicleideResources();
    startBuilds();
    launchApplication();
    return OphicleideWebUI.getInstance(openshift.appDefaultHostNameBuilder("ophicleide-web"));
  }

  // Ensure Mongodb/sparkclusters pods are ready
  private static void waitForSetup(){
    String masterName = CLUSTER_NAME + "-m";
    String workerName = CLUSTER_NAME + "-w";
    WaitUtil.waitForPodsToReachRunningState("deploymentconfig", masterName, 1);
    WaitUtil.waitForPodsToReachRunningState("deploymentconfig", workerName, WORKERS_COUNT);
    WaitUtil.waitForPodsToReachRunningState("name", MONGODB_APP_NAME, 1);
  }

  private static void initializeOphicleideResources(){
    String buildConfigTraining = "/ophicleide/buildConfig-training.yaml";
    String buildConfigWeb = "/ophicleide/buildConfig-web.yaml";
    String imageStreamTraining = "/ophicleide/imagestream-training.yaml";
    String imageStreamWeb = "/ophicleide/imagestream-web.yaml";

    // Load ImageStreamConfig
    openshift.withAdminUser(client ->
      client.imageStreams().inNamespace(NAMESPACE).load(Ophicleide.class.getResourceAsStream(imageStreamTraining)).create()
    );
    openshift.withAdminUser(client ->
      client.imageStreams().inNamespace(NAMESPACE).load(Ophicleide.class.getResourceAsStream(imageStreamWeb)).create()
    );

    // Load BuildConfigs
    openshift.withAdminUser(client ->
      client.buildConfigs().inNamespace(NAMESPACE).load(Ophicleide.class.getResourceAsStream(buildConfigTraining)).create()
    );
    openshift.withAdminUser(client ->
      client.buildConfigs().inNamespace(NAMESPACE).load(Ophicleide.class.getResourceAsStream(buildConfigWeb)).create()
    );


  }

  private static void startBuilds(){
    // Run Builds
    openshift.withAdminUser(client ->
      client.buildConfigs().inNamespace(NAMESPACE).withName("ophicleide-web").instantiate(new BuildRequestBuilder()
        .withNewMetadata()
        .withName("ophicleide-web")
        .endMetadata()
        .build())
    );

    openshift.withAdminUser(client ->
      client.buildConfigs().inNamespace(NAMESPACE).withName("ophicleide-training").instantiate(new BuildRequestBuilder()
        .withNewMetadata()
        .withName("ophicleide-training")
        .endMetadata()
        .build())
    );

    // Wait for builds to complete
    BooleanSupplier successCondition = () -> openshift.getBuilds().stream().filter(
      build -> build.getStatus().getPhase().equals("Complete")).count() == openshift.getBuilds().size();

    BooleanSupplier failCondition = () -> openshift.getBuilds().stream().filter(
      build -> build.getStatus().getPhase().equals("Cancelled") || build.getStatus().getPhase().equals("Failed"))
      .count() > 0;

    try {
      WaitUtil.waitFor(successCondition, failCondition, 1000L, TIMEOUT);
    } catch (InterruptedException|TimeoutException e) {
      e.printStackTrace();
    }
  }

  private static void launchApplication() {
    String template = "/ophicleide/template.yaml";
    waitForSetup();
    openshift.withAdminUser(client ->
      client.inNamespace(NAMESPACE).load(Ophicleide.class.getResourceAsStream(template))
        .deletingExisting()
        .createOrReplace()
    );
    WaitUtil.waitForPodsToReachRunningState("name", APP_NAME, 1);
  }


}
