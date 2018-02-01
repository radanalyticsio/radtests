package com.redhat.xpaas.rad.PySparkHDFS.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.jupyter.JupyterWebUI;
import com.redhat.xpaas.jupyter.entity.CodeCell;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PySparkHDFSWebUI extends JupyterWebUI{

  public final WebDriver webDriver;

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
