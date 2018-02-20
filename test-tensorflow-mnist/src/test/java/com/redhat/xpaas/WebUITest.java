package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.MNIST.api.MNISTWebUI;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runners.MethodSorters;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Loggable(project ="mnist")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static MNISTWebUI MNIST;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final int DRAW_ATTEMPTS = 10;

  @BeforeClass
  public static void setUP() throws TimeoutException, InterruptedException {
    Setup setup = new Setup();
    WebUITest.MNIST = setup.initializeApplications();
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
    setup.cleanUp();
  }

  @Test
  public void testAVerifyCNNBuild(){
    Assertions.assertThat(openshift.buildCompleted("appName", "tf-cnn")).isTrue();
  }

  @Test
  public void testBVerifyRegBuild(){
    Assertions.assertThat(openshift.buildCompleted("appName", "tf-reg")).isTrue();
  }

  @Test
  public void testCVerifyCNNDeployment(){
    Assertions.assertThat(openshift.podRunning("appName", "tf-cnn")).isTrue();
  }

  @Test
  public void testDVerifyRegDeployment(){
    Assertions.assertThat(openshift.podRunning("appName", "tf-reg")).isTrue();
  }

  @Test
  public void testEVerifyMNISTDeployment(){
    Assertions.assertThat(openshift.podRunning("appid", "mnist-app-mnist-app")).isTrue();
  }

  @Test
  public void testFDrawNumberThree() {
    int numberOfAttempts = DRAW_ATTEMPTS;

    MNIST.loadPage();
    boolean loaded = MNIST.drawThree(3000L, 3);

    numberOfAttempts--;
    while(!loaded && numberOfAttempts > 0){
      MNIST.loadPage();
      if(MNIST.drawThree(3000L, 3)){
        loaded = true;
      }
      numberOfAttempts--;
    }

    Assertions.assertThat(loaded).isTrue();

    Map<Integer, Double> modelOneResults = MNIST.modelResults(1);
    Map<Integer, Double> modelTwoResults = MNIST.modelResults(2);

    Assertions.assertThat(maxEntry(modelOneResults).getKey()).isEqualTo(3);
    Assertions.assertThat(maxEntry(modelTwoResults).getKey()).isEqualTo(3);
  }

  private Map.Entry<Integer, Double> maxEntry(Map<Integer, Double> results){
    Map.Entry<Integer, Double> maxEntry = null;
    for (Map.Entry<Integer, Double> entry : results.entrySet()) {
      if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
        maxEntry = entry;
      }
    }
    return maxEntry;
  }

}

