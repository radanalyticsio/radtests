package com.redhat.xpaas;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.AMQP.api.AMQPWebUI;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static AMQPWebUI AMQP;
  private Logger log = LoggerFactory.getLogger(WebUITest.class);;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  @BeforeClass
  public static void setUP() {
    Setup setup = new Setup();
    WebUITest.AMQP = setup.initializeApplications();
    //WebUITest.AMQP = AMQPWebUI.getInstance(openshift.appDefaultHostNameBuilder("AMQP-app"));
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
  }

  @Test
  public void testA(){
    log.info("First test");
  }

}

