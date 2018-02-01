package com.redhat.xpaas.rad.BlockChainAnalysis.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.sparknotebook.SparkNotebookWebUI;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.WebDriver;

public class BlockChainAnalysisSparkWebUI extends SparkNotebookWebUI{

  public static BlockChainAnalysisSparkWebUI getInstance(String hostname) {
    return new BlockChainAnalysisSparkWebUI(TestUtil.createDriver(RadConfiguration.useHeadlessForTests()), hostname);
  }

  private BlockChainAnalysisSparkWebUI(WebDriver webDriver, String hostname) {
    super(webDriver, hostname);
  }

}
