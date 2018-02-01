package com.redhat.xpaas;

import com.redhat.xpaas.logger.LogWrapper;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.PySparkHDFS.api.PySparkHDFSWebUI;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static PySparkHDFSWebUI PySparkHDFS;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String SPARK_MASTER_URL = RadConfiguration.sparkMasterURL();
  LogWrapper log = new LogWrapper(Setup.class, "pysparkhdfs");
  private static final String HDFS_HOST = RadConfiguration.HadoopHost();
  private static final String HDFS_PORT = RadConfiguration.HadoopPort();
  private static final String HDFS_PATH = RadConfiguration.HadoopPath();

  @Rule
  public TestRule watcher = log.getLogTestWatcher();


  @BeforeClass
  public static void setUP() {
    Setup setup = new Setup();
    WebUITest.PySparkHDFS = setup.initializeApplications();
    //WebUITest.PySparkHDFS = PySparkHDFSWebUI.getInstance(openshift.appDefaultHostNameBuilder("base-notebook"));
    PySparkHDFS.login("developer");
    PySparkHDFS.loadProjectByURL("PySpark_HDFS.ipynb");
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
    setup.cleanUp();
  }

  @Test
  public void testAVerifyDeployment(){
    Assertions.assertThat(openshift.podRunning("app", "base-notebook")).isTrue();
  }

  @Test
  public void testBConnectSparkCluster(){
    PySparkHDFS.getNthCodeCell(1).findAndReplaceInCell("spark://mycluster:7077", SPARK_MASTER_URL);
    Assertions.assertThat(PySparkHDFS.getNthCodeCell(1).runCell().outputHasErrors()).isFalse();
  }

  @Test
  public void testBSetTheHDFSConfig(){
    PySparkHDFS.getNthCodeCell(2).findAndReplaceInCell("myhost.me.com", HDFS_HOST);
    PySparkHDFS.getNthCodeCell(2).findAndReplaceInCell("8020", HDFS_PORT);
    PySparkHDFS.getNthCodeCell(2).findAndReplaceInCell("/user/me/input", HDFS_PATH);

    Assertions.assertThat(PySparkHDFS.getNthCodeCell(2).runCell().outputHasErrors()).isFalse();
  }

  @Test
  public void testCReadFileAndPrintCounts(){
    Assertions.assertThat(PySparkHDFS.getNthCodeCell(3).runCell().outputHasErrors()).isFalse();
  }

}

