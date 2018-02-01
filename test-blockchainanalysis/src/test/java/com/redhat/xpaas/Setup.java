package com.redhat.xpaas;

import com.redhat.xpaas.logger.LogWrapper;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.api.OshinkoWebUI;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.BlockChainAnalysis.api.BlockChainAnalysisSparkWebUI;
import com.redhat.xpaas.rad.BlockChainAnalysis.api.BlockChainAnalysisWebUI;
import com.redhat.xpaas.util.Tuple;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.kubernetes.api.model.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static com.redhat.xpaas.rad.BlockChainAnalysis.deployment.BlockChainAnalysis.deployBlockChainAnalysis;
import static com.redhat.xpaas.rad.BlockChainAnalysis.deployment.BlockChainAnalysisSpark.deployBlockChainAnalysisSpark;

public class Setup {
  LogWrapper log = new LogWrapper(Setup.class, "blockchain");
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static BlockChainAnalysisWebUI BlockChainAnalysis;
  private static BlockChainAnalysisSparkWebUI BlockChainAnalysisSpark;

  Tuple<BlockChainAnalysisWebUI, BlockChainAnalysisSparkWebUI> initializeApplications() {
    log.action("creating-new-namespace", this::initializeProject);
    log.action("deploy-blockchain", () -> BlockChainAnalysis = deployBlockChainAnalysis());
    log.action("deploy-blockchain-spark", () -> BlockChainAnalysisSpark = deployBlockChainAnalysisSpark());
    return new Tuple<>(BlockChainAnalysis, BlockChainAnalysisSpark);
  }

  void cleanUp() {
    if(BlockChainAnalysis != null){
      log.action("shutting-down-blockchainanalysis-webdrivers", () -> BlockChainAnalysis.webDriverCleanup());
    }

    if(BlockChainAnalysisSpark != null){
      log.action("shutting-down-blockchainanalysisspark-webdrivers", () -> BlockChainAnalysisSpark.webDriverCleanup());
    }

    log.action("deleting-namespace", () -> openshift.deleteProject(NAMESPACE));
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, true);
  }

}
