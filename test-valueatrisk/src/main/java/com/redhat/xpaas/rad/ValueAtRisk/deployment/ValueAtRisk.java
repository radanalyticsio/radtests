package com.redhat.xpaas.rad.ValueAtRisk.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.ValueAtRisk.api.ValueAtRiskWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ValueAtRisk {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static ValueAtRiskWebUI deployValueAtRisk() throws TimeoutException, InterruptedException {
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

    if(!WaitUtil.waitForPodsToReachRunningState("app", "workshop-notebook", 1)){
      throw new IllegalStateException(LoggerUtil.openshiftError("workshop-notebook deployment", "pods"));
    }

    return ValueAtRiskWebUI.getInstance(openshift.appDefaultHostNameBuilder("workshop-notebook"));
  }

}
