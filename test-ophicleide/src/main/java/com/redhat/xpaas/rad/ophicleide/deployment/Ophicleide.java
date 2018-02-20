package com.redhat.xpaas.rad.ophicleide.deployment;

import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.ophicleide.api.OphicleideWebUI;
import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.BuildRequestBuilder;
import io.fabric8.openshift.api.model.Template;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.wait.WaitUtil.waitForActiveBuildsToComplete;

public class Ophicleide {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String APP_NAME = RadConfiguration.ophicleideAppName();
  private static final Long TIMEOUT = RadConfiguration.timeout();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static OphicleideWebUI deployOphicleideWebUI() throws TimeoutException, InterruptedException {
    initializeOphicleideResources();
    startBuilds();
    launchApplication();
    return OphicleideWebUI.getInstance(openshift.appDefaultHostNameBuilder("ophicleide-web"));
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

  private static void startBuilds() throws TimeoutException, InterruptedException {
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

    if(!waitForActiveBuildsToComplete()){
      throw new IllegalStateException(LoggerUtil.openshiftError("ophicleide builds", "build"));
    }
  }

  private static void launchApplication() throws TimeoutException, InterruptedException {
    String ophResource = "/ophicleide/template.yaml";
    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(Ophicleide.class.getResourceAsStream(ophResource)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("SPARK", RadConfiguration.sparkMasterURL());
    parameters.put("MONGO", RadConfiguration.mongodbURL());

    openshift.loadTemplate(template, parameters);

    if(!WaitUtil.waitForPodsToReachRunningState("name", APP_NAME, 1)){
      throw new IllegalStateException(LoggerUtil.openshiftError("ophicleide deployment", "pods"));
    }
  }
}
