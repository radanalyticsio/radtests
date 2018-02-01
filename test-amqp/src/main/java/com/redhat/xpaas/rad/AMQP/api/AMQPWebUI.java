package com.redhat.xpaas.rad.AMQP.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AMQPWebUI implements AMQPAPI {

  private final static Long TIMEOUT = RadConfiguration.timeout();
  private final String HOSTNAME;
  private final WebDriver webDriver;

  public static AMQPWebUI getInstance(String hostname) {
    return new AMQPWebUI(hostname);
  }

  private AMQPWebUI(String hostname) {
    this.HOSTNAME = hostname;
    webDriver = TestUtil.createDriver(RadConfiguration.useHeadlessForTests());
  }

  @Override
  public void loadPage(){
    String url = "http://" + HOSTNAME;
    webDriver.get(url);
    By canvas = By.id("main");
    TestUtil.pageLoaded(canvas, 2000L, webDriver);
  }


}
