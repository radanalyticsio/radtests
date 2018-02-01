package com.redhat.xpaas;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.api.OshinkoWebUI;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.AMQP.api.AMQPWebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.xpaas.rad.AMQP.deployment.AMQP.deployArtemis;

public class Setup {
  private Logger log = LoggerFactory.getLogger(Setup.class);
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private final String oshinkoClusterName = RadConfiguration.clusterName();
  private final int oshinkoWorkersCount =  RadConfiguration.oshinkoInitialWorkerCount();
  private AMQPWebUI AMQP;

  AMQPWebUI initializeApplications() {
    Logger log = LoggerFactory.getLogger(WebUITest.class);
    log.info("action=creating-new-namespace status=START");
    initializeProject();
    log.info("action=creating-new-namespace status=FINISH");

    log.info("action=starting-oshinko-instance status=START");
    OshinkoWebUI oshinko = Oshinko.deployWebUIPod();
    log.info("action=starting-oshinko-instance status=FINISH");

    log.info("action=launching-spark-cluster status=START");
    Boolean succeeded = oshinko.createCluster(oshinkoClusterName, oshinkoWorkersCount);

    if(!succeeded){
      log.error("action=launching-spark-cluster status=FAILED");
      System.exit(1);
    }

    log.info("action=deploy-AMQP status=START");
    AMQP = deployArtemis();
    log.info("action=deploy-AMQP status=FINISH");

    return AMQP;
  }

  void cleanUp() {
    log.info("action=deleting-namespace status=START");
    log.info("action=shutting-down-webdrivers status=FINISH");
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, true);
  }

}
