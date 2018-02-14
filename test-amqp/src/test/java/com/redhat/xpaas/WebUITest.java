package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.AMQP.api.AMQPWebUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

@Loggable(project ="amq")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static AMQPWebUI AMQP;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  @BeforeClass
  public static void setUP() throws TimeoutException, InterruptedException {
    Setup setup = new Setup();
    WebUITest.AMQP = setup.initializeApplications();
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
    setup.cleanUp();
  }

}

