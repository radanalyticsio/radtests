package com.redhat.xpaas.rad.PySparkHDFS.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.PySparkHDFS.api.PySparkHDFSWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class PySparkHDFS {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static PySparkHDFSWebUI deployPySparkHDFS() throws TimeoutException, InterruptedException {
    String PySparkHDFSTemplate = "/pysparkhdfs/template.yaml";

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(PySparkHDFS.class.getResourceAsStream(PySparkHDFSTemplate)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("PROJECT_NAME", NAMESPACE);
    parameters.put("HOST", RadConfiguration.HostIP());
    parameters.put("SUFFIX", RadConfiguration.RouteSuffix());
    parameters.put("APP_NAME", "base-notebook");

    openshift.loadTemplate(template, parameters);

    boolean succeeded = WaitUtil.waitForPodsToReachRunningState("app", "base-notebook", 1);
    if(!succeeded){
      throw new IllegalStateException(LoggerUtil.openshiftError("base-notebook deployment", "pod"));
    }

    return PySparkHDFSWebUI.getInstance(openshift.appDefaultHostNameBuilder("base-notebook"));
  }
}
