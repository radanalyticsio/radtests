package com.redhat.xpaas.rad.mongodb.deployment;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MongoDB {
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final String MONGODB_TEMPLATE = "/mongoDBDeploymentConfig.yaml";
  private static final String APP_NAME = RadConfiguration.mongodbAppName();
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  public static void deployMongoDBPod(){
    Template[] mongoDB = new Template[1];
    loadMongoResources();
    WaitUtil.waitForPodsToReachRunningState("name", APP_NAME, 1);
  }

  public static void loadMongoResources(){
    openshift.withAdminUser(client ->
      client.inNamespace(NAMESPACE).load(MongoDB.class.getResourceAsStream(MONGODB_TEMPLATE)).createOrReplace()
    );
  }



}
