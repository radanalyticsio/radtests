package com.redhat.xpaas;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.S3Source.api.S3SourceWebUI;
import com.redhat.xpaas.util.TestUtil;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static S3SourceWebUI S3Source;
  private Logger log = LoggerFactory.getLogger(WebUITest.class);;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  @Rule
  public TestRule watcher = new TestWatcher() {
    protected void starting(Description description) {
      log.info("project=s3source action=starting-test-" + description.getMethodName() + " status=START");
    }

    @Override
    protected void failed(Throwable e, Description description) {
      log.info("project=s3source action=starting-test-" + description.getMethodName() + " status=FAILED");
    }

    @Override
    protected void succeeded(Description description) {
      log.info("project=s3source action=starting-test-" + description.getMethodName() + " status=PASSED");
    }
  };

  @BeforeClass
  public static void setUp() {
    Setup setup = new Setup();
    WebUITest.S3Source = setup.initializeApplications();
    //WebUITest.S3Source = S3SourceWebUI.getInstance(openshift.appDefaultHostNameBuilder("base-notebook"));
    S3Source.login("developer");
    S3Source.loadProjectByURL("s3-source-example.ipynb");
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
  public void testBLoadClientLib(){
    assertCodeCellRange(1, 2);
  }

  @Test
  public void testCConfigS3ClientWithCredentials(){
    assertCodeCellRange(3, 4);
  }

  @Test
  public void testDReadTextFileFromS3(){
    assertCodeCellRange(5, 8);
  }

  @Test
  public void testReadParquetFileFromS3(){
    assertCodeCellRange(9, 14);
  }

  private void assertCodeCellRange(int start, int end){
    for(int n = start; n <= end; n++){
      Assertions.assertThat(S3Source.getNthCodeCell(n).runCell().outputHasErrors()).isFalse();
    }
  }

}

