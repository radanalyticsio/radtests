package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.PySparkHDFS.api.PySparkHDFSWebUI;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

@Loggable(project="pysparkhdfs")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static PySparkHDFSWebUI PySparkHDFS;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String SPARK_MASTER_URL = RadConfiguration.sparkMasterURL();
  private static final String HDFS_HOST = RadConfiguration.HadoopHost();
  private static final String HDFS_PORT = RadConfiguration.HadoopPort();
  private static final String HDFS_PATH = RadConfiguration.HadoopPath();

  @BeforeClass
  public static void setUP() throws TimeoutException, InterruptedException {
    Setup setup = new Setup();
    WebUITest.PySparkHDFS = setup.initializeApplications();
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
    assertCodeCell(1);
  }

  @Test
  public void testBSetTheHDFSConfig(){
    PySparkHDFS.getNthCodeCell(2).findAndReplaceInCell("myhost.me.com", HDFS_HOST);
    PySparkHDFS.getNthCodeCell(2).findAndReplaceInCell("8020", HDFS_PORT);
    PySparkHDFS.getNthCodeCell(2).findAndReplaceInCell("/user/me/input", HDFS_PATH);

    assertCodeCell(2);
  }

  @Test
  public void testCReadFileAndPrintCounts(){
    assertCodeCell(3);
  }

  private void assertCodeCell(int cellIndex){
    boolean outputHasErrors;
    try {
      outputHasErrors = PySparkHDFS.getNthCodeCell(cellIndex).runCell().outputHasErrors();
      Assertions.assertThat(outputHasErrors).as("Check output status of cell %s", cellIndex).isFalse();
    } catch (AssertionError e) {
      Assertions.assertThat(e).hasMessage(String.format("Expected:<false> but was <%s>. With outputmessage: %s",
        false, PySparkHDFS.getNthCodeCell(cellIndex).runCell().getOutput()));
    }
  }
}

