package com.redhat.xpaas.rad.wordfountain.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.openshift.api.model.BuildRequestBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

public class WordFountain {

  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final Long TIMEOUT = RadConfiguration.timeout();

  public static void deployWordFountain() {
    loadResources();
  }

  public static void loadResources() {
    String buildConfig = "/word-fountain/build.yaml";
    String stream = "/word-fountain/imgstream-wordfountain.yaml";
    String py27ImStream = "/word-fountain/imgstream-python27.yaml";
    String service = "/word-fountain/service.yaml";
    String config = "/word-fountain/config.yaml";

    String service_account = "word-fountain";
    ServiceAccount oshinko_sa = new ServiceAccountBuilder().withNewMetadata().withName(service_account).endMetadata().build();
    openshift.withAdminUser(client -> client.serviceAccounts().inNamespace(NAMESPACE).create(oshinko_sa));
    openshift.addRoleToServiceAccount("system:image-puller", service_account);

    openshift.withAdminUser(client ->
      client.services().inNamespace(NAMESPACE).load(WordFountain.class.getResourceAsStream(service)).createOrReplace()
    );

    openshift.withAdminUser(client ->
      client.imageStreams().inNamespace(NAMESPACE).load(WordFountain.class.getResourceAsStream(stream)).createOrReplace()
    );

    openshift.withAdminUser(client ->
      client.imageStreams().inNamespace(NAMESPACE).load(WordFountain.class.getResourceAsStream(py27ImStream)).createOrReplace()
    );


    openshift.withAdminUser(client ->
      client.buildConfigs().inNamespace(NAMESPACE).load(WordFountain.class.getResourceAsStream(buildConfig)).createOrReplace()
    );

    WaitUtil.waitForActiveBuildsToComplete();

    DeploymentConfig dc = openshift.withAdminUser(client ->
      client.deploymentConfigs().inNamespace(NAMESPACE).load(WordFountain.class.getResourceAsStream(config)).create()
    );


  }
}
