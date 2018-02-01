package com.redhat.xpaas;

import com.redhat.xpaas.logger.LogWrapper;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.mongodb.deployment.MongoDB;
import com.redhat.xpaas.rad.ophicleide.api.OphicleideWebUI;
import com.redhat.xpaas.oshinko.api.OshinkoWebUI;
import com.redhat.xpaas.oshinko.deployment.Oshinko;
import com.redhat.xpaas.rad.ophicleide.deployment.Ophicleide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Setup {
  LogWrapper log = new LogWrapper(Setup.class, "ophicleide");
  private String NAMESPACE = RadConfiguration.masterNamespace();
  private OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private final String oshinkoClusterName = RadConfiguration.clusterName();
  private final int oshinkoWorkersCount =  RadConfiguration.oshinkoInitialWorkerCount();
  private static OshinkoWebUI oshinko;
  private static OphicleideWebUI ophicleide;

  OphicleideWebUI initializeApplications() {
    log.action("creating-new-namespace", this::initializeProject);
    log.action("starting-oshinko-instance", () -> oshinko = Oshinko.deployWebUIPod());
    log.action("launching-spark-cluster", () -> oshinko.createCluster(oshinkoClusterName, oshinkoWorkersCount));
    log.action("starting-mongodb-instance", MongoDB::deployMongoDBPod);
    log.action("starting-mongodb-instance", () -> ophicleide = Ophicleide.deployOphicleideWebUI());
    return ophicleide;
  }

  void cleanUp() {

    if(ophicleide != null){
      log.action("shutting-down-ophicleide-webdrivers", () -> ophicleide.webDriverCleanup());
    }

    if(oshinko != null){
      log.action("shutting-down-oshinko-webdrivers", () -> oshinko.webDriverCleanup());
    }

    log.action("deleting-namespace", () -> openshift.deleteProject(NAMESPACE));

  }

  private void initializeProject(){
    OpenshiftUtil.getInstance().createProject(NAMESPACE, true);
  }

}
