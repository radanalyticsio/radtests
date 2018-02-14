package com.redhat.xpaas.rad.S3Source.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.jupyter.JupyterWebUI;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.WebDriver;

public class S3SourceWebUI extends JupyterWebUI{
  public static S3SourceWebUI getInstance(String hostname) {
    return new S3SourceWebUI(TestUtil.createDriver(RadConfiguration.useHeadlessForTests()), hostname);
  }

  private S3SourceWebUI(WebDriver webDriver, String hostname) {
    super(webDriver, hostname);
  }

}
