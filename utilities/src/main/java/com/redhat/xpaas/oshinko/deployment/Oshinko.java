package com.redhat.xpaas.oshinko.deployment;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.oshinko.api.OshinkoWebUI;
import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.Template;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@Loggable(project = "oshinko")
public class Oshinko {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String APP_NAME = RadConfiguration.oshinkoAppName();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final String ROUTE_SUFFIX = RadConfiguration.RouteSuffix();
  private static final String HOST_IP = RadConfiguration.HostIP();
  private static final String OSHINKO_TEMPLATE = "/oshinko/oshinko-webui-template.yaml";
  private static final String PYSPARK_TEMPLATE = "/oshinko/pyspark-template.yaml";
  private static final String SCALASPARK_TEMPLATE = "/oshinko/scala-spark-template.yaml";
  private static final String JAVASPARK_TEMPLATE = "/oshinko/java-spark-template.yaml";
  private static final String ROUTES = "/oshinko/routes.yaml";
  private static final String CLUSTER_NAME = RadConfiguration.clusterName();
  private static final int WORKERS_COUNT = RadConfiguration.oshinkoInitialWorkerCount();
  private static Template JavaSpark = null;
  private static Template PySpark = null;
  private static Template ScalaSpark = null;


  /**
   * Will deploy webUI pod for Oshinko and waits till ready to handle requests.
   */
  public static OshinkoWebUI deployWebUIPod() throws TimeoutException, InterruptedException {
    createServiceAccount("edit");
    loadWebUIResources();

    try {
      WaitUtil.waitFor(WaitUtil.isAPodReady("name", APP_NAME));
    } catch (InterruptedException | TimeoutException e) {
      throw new IllegalStateException("Timeout expired while waiting for Oshinko server availability.");
    }

    WaitUtil.waitForRoute("oshinko-web", 10000L);
    WaitUtil.waitForRoute("oshinko-web-proxy", 10000L);

    return OshinkoWebUI.getInstance(openshift.appDefaultHostNameBuilder("oshinko-web"));
  }

  public static void deploySparkFromResource() throws TimeoutException, InterruptedException {
    String sparkResource = "/oshinko/spark-metrics-template.yaml";

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(Oshinko.class.getResourceAsStream(sparkResource)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("MASTER_NAME", "spark-m");
    parameters.put("WORKER_NAME", "spark-w");

    openshift.loadTemplate(template, parameters);

    boolean succeeded = WaitUtil.waitForPodsToReachRunningState("name", "spark-m", 1);
    if(!succeeded){
      throw new IllegalStateException(LoggerUtil.openshiftError("spark-m deployment", "pod"));
    }

    succeeded = WaitUtil.waitForPodsToReachRunningState("name", "spark-w", 2);
    if(!succeeded){
      throw new IllegalStateException(LoggerUtil.openshiftError("spark-w deployment", "pod"));
    }
  }

  public static void buildRoute(){
    Map<String, String> parameters = new HashMap<>();
    parameters.put("HOST_IP", HOST_IP);
    parameters.put("NAMESPACE", NAMESPACE);
    parameters.put("ROUTE_SUFFIX", ROUTE_SUFFIX);

    Template template = openshift.withAdminUser(client ->
      client.templates()
        .inNamespace(NAMESPACE).load(Oshinko.class.getResourceAsStream(ROUTES)).createOrReplace()
    );

    openshift.loadTemplate(template, parameters);

  }

  public static void createServiceAccount(String role){
    String service_account = RadConfiguration.oshinkoServiceAccount();
    ServiceAccount oshinko_sa = new ServiceAccountBuilder().withNewMetadata().withName(service_account).endMetadata().build();
    openshift.withAdminUser(client -> client.serviceAccounts().inNamespace(NAMESPACE).createOrReplace(oshinko_sa));
    openshift.addRoleToServiceAccount(role, service_account);
  }

  public static void loadWebUIResources(){
    buildRoute();

    openshift.withAdminUser(client ->
      client.inNamespace(NAMESPACE).load(Oshinko.class.getResourceAsStream(OSHINKO_TEMPLATE))
        .deletingExisting()
        .createOrReplace()
    );
  }

  public static void loadPySparkResources(){
    PySpark = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(Oshinko.class.getResourceAsStream(PYSPARK_TEMPLATE)).createOrReplace()
    );
  }

  public static void loadJavaSparkResources(){
    JavaSpark = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(Oshinko.class.getResourceAsStream(JAVASPARK_TEMPLATE)).createOrReplace()
    );
  }

  public static void loadScalaSparkResources(){
    ScalaSpark = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(Oshinko.class.getResourceAsStream(SCALASPARK_TEMPLATE)).create()
    );
  }

  public static void deployScalaSpark(String appName, String gitURI, String appMainClass, String appArgs, String sparkOptions){
    if (ScalaSpark == null) {
      loadScalaSparkResources();
    }
    Map<String, String> parameters = new HashMap<>();
    parameters.put("APPLICATION_NAME", appName);
    parameters.put("GIT_URI", gitURI);
    parameters.put("APP_MAIN_CLASS", appMainClass);
    parameters.put("APP_ARGS", appArgs);
    parameters.put("SPARK_OPTIONS", sparkOptions);

    openshift.loadTemplate(ScalaSpark, parameters);
  }

  public static void deployJavaSpark(String appName, String gitURI, String appMainClass, String appArgs, String sparkOptions){
    if (JavaSpark == null) {
      loadJavaSparkResources();
    }
    Map<String, String> parameters = new HashMap<>();
    parameters.put("APPLICATION_NAME", appName);
    parameters.put("GIT_URI", gitURI);
    parameters.put("APP_MAIN_CLASS", appMainClass);
    parameters.put("APP_ARGS", appArgs);
    parameters.put("SPARK_OPTIONS", sparkOptions);

    openshift.loadTemplate(JavaSpark, parameters);
  }

  public static void deployPySparkSpark(String appName, String gitURI, String appArgs, String sparkOptions){
    if (PySpark == null) {
      loadPySparkResources();
    }
    Map<String, String> parameters = new HashMap<>();
    parameters.put("APPLICATION_NAME", appName);
    parameters.put("GIT_URI", gitURI);
    parameters.put("APP_ARGS", appArgs);
    parameters.put("SPARK_OPTIONS", sparkOptions);

    openshift.loadTemplate(PySpark, parameters);
  }

  public static void waitForClustersSetup() throws TimeoutException, InterruptedException {
    String masterName = CLUSTER_NAME + "-m";
    String workerName = CLUSTER_NAME + "-w";
    WaitUtil.waitFor(_areNClusterPodsReady(masterName, 1));
    WaitUtil.waitFor(_areNClusterPodsReady(workerName, WORKERS_COUNT));
  }

  private static BooleanSupplier _areNClusterPodsReady(String name, int n){
    Predicate<Pod> podFilter = pod -> pod.getMetadata().getName().startsWith(name);
    return () -> openshift.getPods().stream().filter(podFilter).count() == n;
  }
}
