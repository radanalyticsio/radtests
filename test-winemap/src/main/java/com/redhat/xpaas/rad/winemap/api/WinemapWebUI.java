package com.redhat.xpaas.rad.winemap.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.*;

import java.util.function.BooleanSupplier;


public class WinemapWebUI implements WinemapAPI{
  private final String HOSTNAME;
  private final WebDriver webDriver;
  private final static Long TIMEOUT = RadConfiguration.timeout();

  public static WinemapWebUI getInstance(String hostname) {
    return new WinemapWebUI(hostname);
  }

  private WinemapWebUI(String hostname){
    this.HOSTNAME = hostname;
    webDriver = TestUtil.createDriver(RadConfiguration.useHeadlessForTests());
  }

  public boolean loadPage() {
    String url = "http://" + HOSTNAME;
    webDriver.get(url);
    By byMainPage = By.cssSelector("div.plot-container");
    BooleanSupplier successCondition = () -> webDriver.findElements(byMainPage).size() > 0;
    return TestUtil.pageLoaded(2000L, webDriver, url, successCondition);
  }

  public void webDriverCleanup(){
    webDriver.quit();
  }

}



