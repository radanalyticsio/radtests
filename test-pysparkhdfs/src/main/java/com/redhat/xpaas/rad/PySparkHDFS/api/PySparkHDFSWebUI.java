package com.redhat.xpaas.rad.PySparkHDFS.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.jupyter.JupyterWebUI;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.WebDriver;

public class PySparkHDFSWebUI extends JupyterWebUI{

  private final WebDriver webDriver;

  public static PySparkHDFSWebUI getInstance(String hostname) {
    return new PySparkHDFSWebUI(TestUtil.createDriver(RadConfiguration.useHeadlessForTests()), hostname);
  }

  public void webDriverCleanup(){
    webDriver.quit();
  }

  private PySparkHDFSWebUI(WebDriver webDriver, String hostname) {
    super(webDriver, hostname);
    this.webDriver = webDriver;
  }

}
