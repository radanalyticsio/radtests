package com.redhat.xpaas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class RadConfiguration {

  private static final String OPENSHIFT_VERSION = "com.redhat.xpaas.openshift.version";
  private static final String MASTER_URL = "com.redhat.xpaas.config.master.url";
  private static final String MASTER_USERNAME = "com.redhat.xpaas.config.master.username";
  private static final String MASTER_PASSWORD = "com.redhat.xpaas.config.master.password";
  private static final String MASTER_TOKEN = "com.redhat.xpaas.config.master.token";
  private static final String MASTER_NAMESPACE = "com.redhat.xpaas.config.master.namespace";
  private static final String USE_TOKEN = "com.redhat.xpaasqe.utilities.config.openshift.use.token";
  private static final String DEFAULT_WAIT_TIMEOUT = "com.redhat.xpaasqe.rad.config.timeout";
  private static final String WEB_DRIVER = "com.redhat.xpaasqe.rad.config.webdriver";
  private static final String WEB_DRIVER_PATH = "com.redhat.xpaasqe.rad.config.webdriver.path";
  private static final String MODEL_NAME = "com.redhat.xpaasqe.rad.ophicleide.config.model.name";
  private static final String MODEL_URL = "com.redhat.xpaasqe.rad.ophicleide.config.model.url";
  private static final String SPARK_MASTER_URL = "com.redhat.xpaasqe.rad.oshinko.config.oshinko.masterurl";
  private static final String OSHINKO_APP_NAME = "com.redhat.xpaasqe.rad.oshinko.config.app.name";
  private static final String OPHICLEIDE_APP_NAME = "com.redhat.xpaasqe.rad.ophicleide.config.ophicleide.app.name";
  private static final String OSHINKO_SERVICE_ACCOUNT = "com.redhat.xpaasqe.rad.oshinko.config.service.account";
  private static final String SPARK_CLUSTER_NAME = "com.redhat.xpaasqe.rad.oshinko.config.sparkcluster.name";
  private static final String USE_HEADLESS_FOR_TESTS = "com.redhat.xpaasqe.rad.config.use.headless.tests";
  private static final String USE_HEADLESS_FOR_OSHINKO = "com.redhat.xpaasqe.rad.oshinko.config.use.headless";
  private static final String MONGODB_DATABASE_NAME = "com.redhat.xpaasqe.rad.mongoDB.config.mongodb.name";
  private static final String MONGODB_APP_NAME = "com.redhat.xpaasqe.rad.mongoDB.config.mongodb.app.name";
  private static final String OPENSHIFT_ONLINE = "com.redhat.xpaasqe.utilities.config.openshift.online";
  private static final String AUTH_TOKEN = "com.redhat.xpaasqe.utilities.config.auth.token";
  private static final String OSHINKO_INITIAL_WORKER_COUNT = "com.redhat.xpaasqe.rad.oshinko.config.oshinko.initialworkers";
  private static final String QUERY_WORD = "com.redhat.xpaasqe.rad.ophicleide.config.model.queryWord";
  private static final String HOST_IP = "com.redhat.xpaasqe.rad.config.host.ip";
  private static final String ROUTE_SUFFIX = "com.redhat.xpaasqe.rad.config.route.suffix";
  private static final String HADOOP_HOST = "com.redhat.xpaasqe.rad.hadoop.host";
  private static final String HADOOP_PORT = "com.redhat.xpaasqe.rad.hadoop.port";
  private static final String HADOOP_PATH = "com.redhat.xpaasqe.rad.hadoop.path";
  private static final String MAX_HTTP_TRIES = "util.http.maxtries";
  private static final String MAX_LOGIN_TRIES = "com.redhat.xpaas.config.login.maxattempts";
  private static final String HTTP_TIMEOUT = "com.redhat.xpaasqe.rad.config.httptimeout";
  private static final String WAIT_FOR_PROJECT_DELETION_DURING_CLEANUP = "com.redhat.xpaas.config.project.waitfordeletion";
  private static final String DELETE_NAMESPACE_AFTER_TESTS = "com.redhat.xpaas.config.project.cleanupnamespace";
  private static final String RECREATE_NAMESPACE = "com.redhat.xpaas.config.project.recreatenamespace";
  private static final String MONGODB_DATABASE_SERVICE_NAME = "mongodb";
  private static final String MONGODB_USER = "userRQQ";
  private static final String MONGODB_ADMIN_PASSWORD = "6Bv1VddsJYQ18BOl";
  private static final String MONGODB_DATABASE = "ophicleide";

  private static final Logger log = LoggerFactory.getLogger(RadConfiguration.class);
  private final Properties properties = new Properties();

  private static final RadConfiguration INSTANCE = new RadConfiguration();

  private RadConfiguration() {
    copyValues(fromPath("../radtests.properties"));
  }

  public static RadConfiguration get() {
    return INSTANCE;
  }

  public String readValue(final String key) {
    return readValue(key, null);
  }

  public String readValue(final String key, final String defaultValue) {
    return this.properties.getProperty(key, defaultValue);
  }

  private void copyValues(final Properties source) {
    copyValues(source, false);
  }

  private void copyValues(final Properties source, final boolean overwrite) {
    source.stringPropertyNames().stream()
      .filter(key -> overwrite || !this.properties.containsKey(key))
      .forEach(key -> this.properties.setProperty(key, source.getProperty(key)));
  }

  private Properties fromPath(final String path) {
    final Properties props = new Properties();

    final Path propsPath = Paths.get(path)
      .toAbsolutePath();
    if (Files.isReadable(propsPath)) {
      try (InputStream is = Files.newInputStream(propsPath)) {
        props.load(is);
      } catch (final IOException ex) {
        log.warn("Unable to read properties from '{}'", propsPath);
        log.debug("Exception", ex);
      }
    }

    return props;
  }

  public static String openshiftVersion() {
    return getEnvOtherwiseDefault("OPENSHIFT_VERSION", OPENSHIFT_VERSION);
  }

  public static String oshinkoAppName() {
    return getEnvOtherwiseDefault("OSHINKO_APP_NAME", OSHINKO_APP_NAME);
  }

  public static String ophicleideAppName() {
    return getEnvOtherwiseDefault("OPHICLEIDE_APP_NAME", OPHICLEIDE_APP_NAME);

  }

  public static String oshinkoServiceAccount() {
    return getEnvOtherwiseDefault("OSHINKO_SERVICE_ACCOUNT_NAME", OSHINKO_SERVICE_ACCOUNT);
  }

  public static String webDriver() {
    return getEnvOtherwiseDefault("WEB_DRIVER", WEB_DRIVER);
  }

  public static String webDriverPath() {
    return getEnvOtherwiseDefault("WEB_DRIVER_PATH", WEB_DRIVER_PATH);
  }

  public static String modelName() {
    return getEnvOtherwiseDefault("OPHICLEIDE_MODEL_NAME", MODEL_NAME);
  }

  public static String modelURL() {
    return getEnvOtherwiseDefault("OPHICLEIDE_MODEL_URL", MODEL_URL);
  }

  public static String queryWord() {
    return getEnvOtherwiseDefault("OPHICLEIDE_QUERY_WORD", QUERY_WORD);
  }

  public static String mongodbServiceName() {
    return MONGODB_DATABASE_SERVICE_NAME;
  }

  public static String mongodbDatabase() {
    return MONGODB_DATABASE;
  }

  public static String mongodbUserName() {
    return MONGODB_USER;
  }

  public static String mongodbPassword() {
    return MONGODB_ADMIN_PASSWORD;
  }

  public static String mongodbAppName() {
    return getEnvOtherwiseDefault("MONGODB_APP_NAME", MONGODB_APP_NAME);
  }

  public static String mongodbURL(){
    String service =  RadConfiguration.mongodbServiceName();
    String psw = RadConfiguration.mongodbPassword();
    String user = RadConfiguration.mongodbUserName();
    String database = RadConfiguration.mongodbDatabase();
    return String.format("mongodb://%s:%s@%s/%s", user, psw, service, database);
  }
  public static String clusterName() {
    return getEnvOtherwiseDefault("OSHINKO_SPARK_CLUSTER_NAME", SPARK_CLUSTER_NAME);
  }

  public static Boolean useHeadlessForTests() {
    String value = getEnvOtherwiseDefault("USE_HEADLESS_FOR_TESTS", USE_HEADLESS_FOR_TESTS);
    return Boolean.valueOf(value);
  }

  public static Boolean useHeadlessForOshinko() {
    String value = getEnvOtherwiseDefault("USE_HEADLESS_FOR_OSHINKO", USE_HEADLESS_FOR_OSHINKO);
    return Boolean.valueOf(value);
  }

  public static Long timeout() {
    return Long.parseLong(getEnvOtherwiseDefault("DEFAULT_WAIT_TIMEOUT", DEFAULT_WAIT_TIMEOUT));
  }

  public static int oshinkoInitialWorkerCount() {
    return Integer.parseInt(getEnvOtherwiseDefault("OSHINKO_WORKER_COUNT", OSHINKO_INITIAL_WORKER_COUNT));
  }

  public static Boolean openshiftOnline() {
    String value = getEnvOtherwiseDefault("OPENSHIFT_ONLINE", OPENSHIFT_ONLINE);
    return Boolean.valueOf(value);
  }

  public static String AuthToken() {
    return getEnvOtherwiseDefault("OPENSHIFT_AUTH_TOKEN", AUTH_TOKEN);
  }

  public static String HostIP() {
    return getEnvOtherwiseDefault("OPENSHIFT_HOST_IP", HOST_IP);
  }

  public static String RouteSuffix(){
    return getEnvOtherwiseDefault("ROUTE_SUFFIX", ROUTE_SUFFIX);
  }

  public static String HadoopHost(){
    return getEnvOtherwiseDefault("HADOOP_HOST", HADOOP_HOST);
  }

  public static String HadoopPort(){
    return getEnvOtherwiseDefault("HADOOP_PORT", HADOOP_PORT);
  }

  public static String HadoopPath(){
    return getEnvOtherwiseDefault("HADOOP_PATH", HADOOP_PATH);
  }

  public static String sparkMasterURL(){
    return getEnvOtherwiseDefault("OSHINKO_SPARK_MASTER_URL", SPARK_MASTER_URL);
  }

  public static String masterUrl() {
    return getEnvOtherwiseDefault("OPENSHIFT_URL", MASTER_URL);
  }

  public static String masterUsername() {
    return getEnvOtherwiseDefault("OPENSHIFT_USERNAME", MASTER_USERNAME);
  }

  public static String masterPassword() {
    return getEnvOtherwiseDefault("OPENSHIFT_PASSWORD", MASTER_PASSWORD);
  }

  public static String masterNamespace() {
    return getEnvOtherwiseDefault("OPENSHIFT_NAMESPACE", MASTER_NAMESPACE);
  }

  public static String getMasterToken() {
    return getEnvOtherwiseDefault("OPENSHIFT_MASTER_TOKEN", MASTER_TOKEN);
  }

  public static int maxHttpTries() {
    String value = getEnvOtherwiseDefault("MAX_HTTP_TRIES", MAX_HTTP_TRIES);
    return Integer.parseInt(value);
  }

  public static Boolean useToken(){
    String value = getEnvOtherwiseDefault("OPENSHIFT_USE_TOKEN", USE_TOKEN);
    return Boolean.valueOf(value);
  }

  public static int maxLoginTries(){
    return Integer.parseInt(getEnvOtherwiseDefault("MAX_LOGIN_TRIES", MAX_LOGIN_TRIES));
  }

  public static int httpTimeout(){
    return Integer.parseInt(getEnvOtherwiseDefault("HTTP_TIMEOUT", HTTP_TIMEOUT));
  }

  public static Boolean waitForProjectDeletion() {
    String value = getEnvOtherwiseDefault("PROJECT_DELETION_WAIT", WAIT_FOR_PROJECT_DELETION_DURING_CLEANUP, "false");
    return Boolean.valueOf(value);
  }

  public static Boolean deleteNamespaceAfterTests() {
    String value = getEnvOtherwiseDefault("DELETE_NAMESPACE_AFTER_TESTS", DELETE_NAMESPACE_AFTER_TESTS, "false");
    return Boolean.valueOf(value);
  }

  public static Boolean recreateNamespace() {
    String value = getEnvOtherwiseDefault("RECREATE_NAMESPACE", RECREATE_NAMESPACE, "false");
    return Boolean.valueOf(value);
  }

  private static String getEnvOtherwiseDefault(String envName, String configValue){
    String value = System.getenv(envName);
    return value == null? get().readValue(configValue) : value;
  }

  private static String getEnvOtherwiseDefault(String envName, String configValue, String defaultKey){
    String value = System.getenv(envName);
    return value == null? get().readValue(configValue, defaultKey) : value;
  }

}
