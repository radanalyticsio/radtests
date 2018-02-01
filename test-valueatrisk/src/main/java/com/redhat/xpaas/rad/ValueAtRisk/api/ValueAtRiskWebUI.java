package com.redhat.xpaas.rad.ValueAtRisk.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.jupyter.JupyterWebUI;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.WebDriver;

public class ValueAtRiskWebUI extends JupyterWebUI{

  public static ValueAtRiskWebUI getInstance(String hostname) {
    return new ValueAtRiskWebUI(TestUtil.createDriver(RadConfiguration.useHeadlessForTests()), hostname);
  }

  private ValueAtRiskWebUI(WebDriver webDriver, String hostname) {
    super(webDriver, hostname);
  }
}
