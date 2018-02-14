package com.redhat.xpaas.rad.S3Source.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.S3Source.api.S3SourceWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class S3Source {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static S3SourceWebUI deployS3Source() throws TimeoutException, InterruptedException {
    String S3SourceTemplate = "/s3source/template.yaml";

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(S3Source.class.getResourceAsStream(S3SourceTemplate)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("PROJECT_NAME", NAMESPACE);
    parameters.put("HOST", RadConfiguration.HostIP());
    parameters.put("SUFFIX", RadConfiguration.RouteSuffix());
    parameters.put("APP_NAME", "base-notebook");

    openshift.loadTemplate(template, parameters);

    if(!WaitUtil.waitForPodsToReachRunningState("app", "base-notebook", 1)){
      throw new IllegalStateException(LoggerUtil.openshiftError("base-notebook deployment", "pod"));
    }

    return S3SourceWebUI.getInstance(openshift.appDefaultHostNameBuilder("base-notebook"));
  }

}
