package com.redhat.xpaas.rad.BlockChainAnalysis.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.BlockChainAnalysis.api.BlockChainAnalysisWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class BlockChainAnalysis {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static BlockChainAnalysisWebUI deployBlockChainAnalysis() throws TimeoutException, InterruptedException {
    String BlockChainAnalysisTemplate = "/blockchainanalysis/template.yaml";

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(BlockChainAnalysis.class.getResourceAsStream(BlockChainAnalysisTemplate)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("PROJECT_NAME", NAMESPACE);
    parameters.put("HOST", RadConfiguration.HostIP());
    parameters.put("SUFFIX", RadConfiguration.RouteSuffix());
    parameters.put("APP_NAME", "bitcoin-notebook");

    openshift.loadTemplate(template, parameters);

    if(!WaitUtil.waitForPodsToReachRunningState("app", "bitcoin-notebook", 1)){
      throw new IllegalStateException(LoggerUtil.openshiftError("blockchain launch", "pods"));
    }

    return BlockChainAnalysisWebUI.getInstance(openshift.appDefaultHostNameBuilder("bitcoin-notebook"));
  }

}
