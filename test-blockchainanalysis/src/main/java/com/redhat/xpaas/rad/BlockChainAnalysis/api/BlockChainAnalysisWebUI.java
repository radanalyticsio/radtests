package com.redhat.xpaas.rad.BlockChainAnalysis.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.jupyter.JupyterWebUI;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.WebDriver;

public class BlockChainAnalysisWebUI extends JupyterWebUI{

  public static BlockChainAnalysisWebUI getInstance(String hostname) {
    return new BlockChainAnalysisWebUI(TestUtil.createDriver(RadConfiguration.useHeadlessForTests()), hostname);
  }

  private BlockChainAnalysisWebUI(WebDriver webDriver, String hostname) {
    super(webDriver, hostname);
  }

}
