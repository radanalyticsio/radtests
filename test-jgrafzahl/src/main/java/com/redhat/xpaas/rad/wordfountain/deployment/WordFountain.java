package com.redhat.xpaas.rad.wordfountain.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.openshift.api.model.Template;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class WordFountain {

  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final String wordFountainTemplate = "/word-fountain/template.yaml";

  public static boolean deployWordFountain() throws TimeoutException, InterruptedException {
    String service_account = "word-fountain";
    ServiceAccount oshinko_sa = new ServiceAccountBuilder().withNewMetadata().withName(service_account).endMetadata().build();
    openshift.withAdminUser(client -> client.serviceAccounts().inNamespace(NAMESPACE).create(oshinko_sa));
    openshift.addRoleToServiceAccount("system:image-puller", service_account);

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(WordFountain.class.getResourceAsStream(wordFountainTemplate)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("PROJECT_NAME", NAMESPACE);

    openshift.loadTemplate(template, parameters);

    boolean succeeded = WaitUtil.waitFor(WaitUtil.isAPodReady("word-fountain"));

    if(!succeeded){
      throw new IllegalStateException(LoggerUtil.openshiftError("word-fountain deployment", "pod"));
    }

    return true;
  }
}
