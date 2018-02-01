package com.redhat.xpaas.rad.ValueAtRisk.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.ValueAtRisk.api.ValueAtRiskWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;
import java.util.HashMap;
import java.util.Map;

public class ValueAtRisk {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static ValueAtRiskWebUI deployValueAtRisk() {
    String valueAtRiskTemplate = "/valueatrisk/template.yaml";

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(ValueAtRisk.class.getResourceAsStream(valueAtRiskTemplate)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("PROJECT_NAME", NAMESPACE);
    parameters.put("HOST", RadConfiguration.HostIP());
    parameters.put("SUFFIX", RadConfiguration.RouteSuffix());
    parameters.put("APP_NAME", "workshop-notebook");
    openshift.loadTemplate(template, parameters);
    WaitUtil.waitForPodsToReachRunningState("app", "workshop-notebook", 1);

    return ValueAtRiskWebUI.getInstance(openshift.appDefaultHostNameBuilder("workshop-notebook"));
  }

}
