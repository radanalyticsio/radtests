package com.redhat.xpaas;

import com.redhat.xpaas.logger.LogWrapper;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.api.OshinkoWebUI;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.PySparkHDFS.api.PySparkHDFSWebUI;
import com.redhat.xpaas.rad.PySparkHDFS.deployment.PySparkHDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.xpaas.rad.PySparkHDFS.deployment.PySparkHDFS.deployPySparkHDFS;

public class Setup {
  LogWrapper log = new LogWrapper(Setup.class, "blockchain");
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private final String oshinkoClusterName = RadConfiguration.clusterName();
  private final int oshinkoWorkersCount =  RadConfiguration.oshinkoInitialWorkerCount();
  private static PySparkHDFSWebUI pySparkHDFS;
  private static OshinkoWebUI oshinko;

  PySparkHDFSWebUI initializeApplications() {
    log.action("creating-new-namespace", this::initializeProject);
    log.action("starting-oshinko-instance", () -> oshinko = Oshinko.deployWebUIPod());

    log.action("launching-spark-cluster", () -> {
      Boolean succeeded = oshinko.createCluster(oshinkoClusterName, oshinkoWorkersCount);
      if(!succeeded){
        log.error("launching-spark-cluster");
        System.exit(1);
      }
      Oshinko.waitForClustersSetup();
    });

    log.action("starting-oshinko-instance", () -> pySparkHDFS = deployPySparkHDFS());

    return pySparkHDFS;
  }

  void cleanUp() {
    if(pySparkHDFS != null){
      log.action("shutting-down-webdrivers", () -> pySparkHDFS.webDriverCleanup());
    }
    log.action("deleting-namespace", () -> openshift.deleteProject(NAMESPACE));
  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, true);
  }

}
