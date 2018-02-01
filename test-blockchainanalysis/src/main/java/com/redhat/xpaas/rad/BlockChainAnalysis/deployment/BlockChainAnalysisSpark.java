package com.redhat.xpaas.rad.BlockChainAnalysis.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.BlockChainAnalysis.api.BlockChainAnalysisSparkWebUI;

import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;

import java.util.HashMap;
import java.util.Map;

public class BlockChainAnalysisSpark {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static BlockChainAnalysisSparkWebUI deployBlockChainAnalysisSpark() {
    String BlockChainAnalysisTemplateSpark = "/blockchainanalysis/templatespark.yaml";

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(BlockChainAnalysisSpark.class.getResourceAsStream(BlockChainAnalysisTemplateSpark)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("PROJECT_NAME", NAMESPACE);
    parameters.put("HOST", RadConfiguration.HostIP());
    parameters.put("SUFFIX", RadConfiguration.RouteSuffix());
    parameters.put("APP_NAME", "bitcoin-spark-notebook");

    openshift.loadTemplate(template, parameters);
    WaitUtil.waitForPodsToReachRunningState("app", "bitcoin-spark-notebook", 1);

    return BlockChainAnalysisSparkWebUI.getInstance(openshift.appDefaultHostNameBuilder("bitcoin-spark-notebook"));
  }

}
