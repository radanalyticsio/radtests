package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.winemap.api.WinemapWebUI;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

@Loggable(project = "winemap")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static WinemapWebUI winemap;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  @BeforeClass
  public static void setUP() throws TimeoutException, InterruptedException {
    Setup setup = new Setup();
    WebUITest.winemap = setup.initializeApplications();
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
    setup.cleanUp();
  }

  @Test
  public void testAWineMapBuildCompleted() {
    Assertions.assertThat(openshift.buildCompleted("app", "winemap")).isTrue();
  }

  @Test
  public void testBWineMapPodsRunning() {
    Assertions.assertThat(openshift.podRunning("app", "winemap")).isTrue();
  }

  @Test
  public void testCPostgresPodsRunning() {
    Assertions.assertThat(openshift.podRunning("name", "postgresql")).isTrue();
  }

  @Test
  public void testDWinemapRouteIsExposed() {
    Assertions.assertThat(openshift.getRouteStatus("winemap")).isTrue();
  }

  @Test
  public void testEPageSuccessfullyLoads(){
    Assertions.assertThat(winemap.loadPage()).isTrue();
  }

}

