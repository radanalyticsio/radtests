package com.redhat.xpaas.rad.mongodb.deployment;

import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MongoDB {
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final String MONGODB_TEMPLATE = "/mongoDBDeploymentConfig.yaml";
  private static final String APP_NAME = RadConfiguration.mongodbAppName();
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  public static void deployMongoDBPod() throws TimeoutException, InterruptedException {
    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(MongoDB.class.getResourceAsStream(MONGODB_TEMPLATE)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("DATABASE_SERVICE_NAME", RadConfiguration.mongodbServiceName());
    parameters.put("MONGODB_DATABASE", RadConfiguration.mongodbDatabase());
    parameters.put("MONGODB_USER", RadConfiguration.mongodbUserName());
    parameters.put("MONGODB_PASSWORD", RadConfiguration.mongodbPassword());
    parameters.put("MONGODB_ADMIN_PASSWORD",RadConfiguration.mongodbPassword());

    openshift.loadTemplate(template, parameters);

    if(!WaitUtil.waitForPodsToReachRunningState("name", APP_NAME, 1)){
      throw new IllegalStateException(LoggerUtil.openshiftError("mongodb deployment", "pod"));
    }
  }
}
