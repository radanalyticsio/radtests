package com.redhat.xpaas;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.api.OshinkoWebUI;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.CephSource.api.CephSourceWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.kubernetes.api.model.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static com.redhat.xpaas.rad.CephSource.deployment.CephSource.deployCephSource;

public class Setup {
  private Logger log = LoggerFactory.getLogger(Setup.class);
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private final String oshinkoClusterName = RadConfiguration.clusterName();
  private final int oshinkoWorkersCount =  RadConfiguration.oshinkoInitialWorkerCount();
  private static final int WORKERS_COUNT = RadConfiguration.oshinkoInitialWorkerCount();
  private static final String CLUSTER_NAME = RadConfiguration.clusterName();

  CephSourceWebUI initializeApplications() {
    Logger log = LoggerFactory.getLogger(WebUITest.class);
    log.info("action=creating-new-namespace status=START");
    initializeProject();
    log.info("action=creating-new-namespace status=FINISH");

    log.info("action=starting-oshinko-instance status=START");
    OshinkoWebUI oshinko = Oshinko.deployWebUIPod();
    log.info("action=starting-oshinko-instance status=FINISH");

    log.info("action=launching-spark-cluster status=START");
    oshinko.createCluster(oshinkoClusterName, oshinkoWorkersCount);
    waitForClustersSetup();
    log.info("action=launching-spark-cluster status=FINISH");

    log.info("action=deploy-CephSource status=START");
    CephSourceWebUI CephSource = deployCephSource();
    log.info("action=deploy-CephSource status=FINISH");

    return CephSource;
  }

  void cleanUp() {
    log.info("action=deleting-namespace status=START");
    log.info("action=shutting-down-webdrivers status=FINISH");
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, true);
  }

  private static void waitForClustersSetup(){
    String masterName = CLUSTER_NAME + "-m";
    String workerName = CLUSTER_NAME + "-w";
    try {
      WaitUtil.waitFor(_areNClusterPodsReady(masterName, 1));
      WaitUtil.waitFor(_areNClusterPodsReady(workerName, WORKERS_COUNT));
    } catch (InterruptedException | TimeoutException e) {
      throw new IllegalStateException("Timeout expired while waiting for cluster/mongoDB pods or  availability");
    }
  }

  private static BooleanSupplier _areNClusterPodsReady(String name, int n){
    Predicate<Pod> podFilter = pod -> pod.getMetadata().getName().startsWith(name);
    return () -> openshift.getPods().stream().filter(podFilter).count() == n;
  }
}
