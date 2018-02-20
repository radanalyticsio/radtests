package com.redhat.xpaas;

import com.redhat.xpaas.logger.LogWrapper;
import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.AMQP.api.AMQPWebUI;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.AMQP.deployment.AMQP.deployArtemis;

@Loggable(project ="amq")
public class Setup {
  private LogWrapper log = new LogWrapper(Setup.class, "amq");
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static AMQPWebUI AMQP;

  public AMQPWebUI initializeApplications() throws TimeoutException, InterruptedException {
    initializeProject();
    Oshinko.deploySparkFromResource();
    AMQP = deployArtemis();
    return AMQP;
  }

  public void cleanUp() {
    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }

}
