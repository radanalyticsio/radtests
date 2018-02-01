package com.redhat.xpaas.rad.CephSource.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.jupyter.JupyterWebUI;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.WebDriver;

public class CephSourceWebUI extends JupyterWebUI{

  public static CephSourceWebUI getInstance(String hostname) {
    return new CephSourceWebUI(TestUtil.createDriver(RadConfiguration.useHeadlessForTests()), hostname);
  }

  private CephSourceWebUI(WebDriver webDriver, String hostname) {
    super(webDriver, hostname);
  }

}
