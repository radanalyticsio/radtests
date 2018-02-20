package com.redhat.xpaas.rad.CephSource.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.CephSource.api.CephSourceWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class CephSource {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static CephSourceWebUI deployCephSource() throws TimeoutException, InterruptedException {
    String CephSourceTemplate = "/cephsource/template.yaml";

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(CephSource.class.getResourceAsStream(CephSourceTemplate)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("PROJECT_NAME", NAMESPACE);
    parameters.put("HOST", RadConfiguration.HostIP());
    parameters.put("SUFFIX", RadConfiguration.RouteSuffix());
    parameters.put("APP_NAME", "base-notebook");

    openshift.loadTemplate(template, parameters);
    WaitUtil.waitForPodsToReachRunningState("app", "base-notebook", 1);

    return CephSourceWebUI.getInstance(openshift.appDefaultHostNameBuilder("base-notebook"));
  }

}
