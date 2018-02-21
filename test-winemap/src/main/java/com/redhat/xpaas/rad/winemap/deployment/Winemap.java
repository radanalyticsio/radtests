package com.redhat.xpaas.rad.winemap.deployment;

import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.winemap.api.WinemapWebUI;
import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.wait.WaitUtil.waitForActiveBuildsToComplete;

public class Winemap {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static WinemapWebUI deployWinemapWebUI() throws TimeoutException, InterruptedException {
    String winemapDataLoaderTemplate = "/winde-data-loader.yaml";

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(Winemap.class.getResourceAsStream(winemapDataLoaderTemplate)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("PROJECT_NAME", NAMESPACE);
    parameters.put("HOST", RadConfiguration.HostIP());
    parameters.put("SUFFIX", RadConfiguration.RouteSuffix());
    parameters.put("APP_NAME", "winemap");

    openshift.loadTemplate(template, parameters);

    Oshinko.createServiceAccount("edit");

    Map<String, String> envVars = new HashMap<>();
    envVars.put("SERVER", "postgresql");
    envVars.put("DBNAME", "wineDb");
    envVars.put("PASSWORD", "password");
    envVars.put("USER", "username");

    Oshinko.deployPySparkSpark("winemap", "https://github.com/radanalyticsio/winemap.git",
      "",
      "--packages org.postgresql:postgresql:42.1.4", envVars);

    if(!waitForActiveBuildsToComplete()){
      throw new IllegalStateException(LoggerUtil.openshiftError("winemap builds", "build"));
    }

    if(!WaitUtil.waitForPodsToReachRunningState("app", "winemap", 1)){
      throw new IllegalStateException(LoggerUtil.openshiftError("winemap deployment", "pod"));
    }

    if(!WaitUtil.waitForPodsToReachRunningState("oshinko-type", "worker", 1)){
      throw new IllegalStateException(LoggerUtil.openshiftError("oshinko-worker deployment", "pod"));
    }

    if(!WaitUtil.waitForPodsToReachRunningState("oshinko-type", "master", 1)){
      throw new IllegalStateException(LoggerUtil.openshiftError("oshinko-master deployment", "pod"));
    }

    return WinemapWebUI.getInstance(openshift.appDefaultHostNameBuilder("winemap"));
  }
}
