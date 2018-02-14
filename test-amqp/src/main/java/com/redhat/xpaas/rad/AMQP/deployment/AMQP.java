package com.redhat.xpaas.rad.AMQP.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.AMQP.api.AMQPWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.kubernetes.api.model.ReplicationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public class AMQP {
  private static final Logger log = LoggerFactory.getLogger(AMQP.class);
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final Long TIMEOUT = RadConfiguration.timeout();

  public static AMQPWebUI deployArtemis() throws TimeoutException, InterruptedException {

    String ArtemisReplicationControllerConfig = "/artemis-rc.yaml";

    ReplicationController rc = openshift.withAdminUser(client ->
      client.replicationControllers().inNamespace(NAMESPACE).load(AMQP.class.getResourceAsStream(ArtemisReplicationControllerConfig)).createOrReplace()
    );

    WaitUtil.waitForPodsToReachRunningState("name", "artemis", 1);

    return AMQPWebUI.getInstance(openshift.appDefaultHostNameBuilder("AMQP-app"));
  }
}
