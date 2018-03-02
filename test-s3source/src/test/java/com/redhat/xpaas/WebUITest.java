package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.S3Source.api.S3SourceWebUI;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

@Loggable(project ="S3Source")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static S3SourceWebUI S3Source;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  public static boolean p(){
    return false;
  }
  @BeforeClass
  public static void setUp() throws TimeoutException, InterruptedException {
    Setup setup = new Setup();
    WebUITest.S3Source = setup.initializeApplications();
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
  public void testEReadParquetFileFromS3(){
    assertCodeCellRange(9, 14);
  }

  private void assertCodeCellRange(int start, int end){
    boolean outputHasErrors;
    for(int n = start; n <= end; n++){
      try {
        outputHasErrors = S3Source.getNthCodeCell(n).runCell().outputHasErrors();
        Assertions.assertThat(outputHasErrors).as("Check output status of cell %s", n).isFalse();
      } catch (AssertionError e) {
        Assertions.assertThat(e).hasMessage(String.format("Expected:<false> but was <%s>. With outputmessage: %s",
          false, S3Source.getNthCodeCell(n).runCell().getOutput()));
      }
    }
  }

}

