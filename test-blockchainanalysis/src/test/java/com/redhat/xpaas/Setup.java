package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.BlockChainAnalysis.api.BlockChainAnalysisSparkWebUI;
import com.redhat.xpaas.rad.BlockChainAnalysis.api.BlockChainAnalysisWebUI;
import com.redhat.xpaas.util.Tuple;

import java.util.concurrent.TimeoutException;

import static com.redhat.xpaas.rad.BlockChainAnalysis.deployment.BlockChainAnalysis.deployBlockChainAnalysis;
import static com.redhat.xpaas.rad.BlockChainAnalysis.deployment.BlockChainAnalysisSpark.deployBlockChainAnalysisSpark;

@Loggable(project="blockchain")
public class Setup {
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static BlockChainAnalysisWebUI BlockChainAnalysis;
  private static BlockChainAnalysisSparkWebUI BlockChainAnalysisSpark;

  public Tuple<BlockChainAnalysisWebUI, BlockChainAnalysisSparkWebUI> initializeApplications() throws TimeoutException, InterruptedException {
    initializeProject();
    BlockChainAnalysis = deployBlockChainAnalysis();
    BlockChainAnalysisSpark = deployBlockChainAnalysisSpark();
    return new Tuple<>(BlockChainAnalysis, BlockChainAnalysisSpark);
  }

  public void cleanUp() {
    if(BlockChainAnalysis != null){
      BlockChainAnalysis.webDriverCleanup();
    }

    if(BlockChainAnalysisSpark != null){
      BlockChainAnalysisSpark.webDriverCleanup();
    }

    if(RadConfiguration.deleteNamespaceAfterTests()){
      openshift.deleteProject(NAMESPACE);
    }
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, RadConfiguration.recreateNamespace());
  }

}
