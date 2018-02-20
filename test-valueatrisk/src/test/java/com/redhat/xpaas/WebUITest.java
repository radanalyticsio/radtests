package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.ValueAtRisk.api.ValueAtRiskWebUI;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

@Loggable(project = "valueatrisk")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static ValueAtRiskWebUI ValueAtRisk;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  @BeforeClass
  public static void setUp() throws TimeoutException, InterruptedException {
    Setup setup = new Setup();
    WebUITest.ValueAtRisk = setup.initializeApplications();
    ValueAtRisk.login("developer");
    ValueAtRisk.loadProjectByURL("var.ipynb");
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
    setup.cleanUp();
  }

  @Test
  public void testAVerifyDeployment(){
    Assertions.assertThat(openshift.podRunning("app", "workshop-notebook")).isTrue();
  }

  @Test
  public void testBBasicSetup(){
    Assertions.assertThat(ValueAtRisk.getNthCodeCell(1).runCell().outputHasErrors()).isFalse();
  }

  @Test
  public void testCLoadingData(){
    Assertions.assertThat(ValueAtRisk.getNthCodeCell(2).runCell().outputHasErrors()).isFalse();
  }

  @Test
  public void testDCalculatingHistoricalReturns(){
    Assertions.assertThat(ValueAtRisk.getNthCodeCell(3).runCell().outputHasErrors()).isFalse();
  }

  @Test
  public void testEExpectedReturnDist(){
    assertCodeCellRange(4, 5);
  }

  @Test
  public void testFGetSecurityPrices(){
    Assertions.assertThat(ValueAtRisk.getNthCodeCell(6).runCell().outputHasErrors()).isFalse();
  }

  @Test
  public void testGSetSimulation(){
    assertCodeCellRange(7, 14);
  }

  @Test
  public void testHVisualizeRandomWalks(){
    assertCodeCellRange(15, 17);
  }

  @Test
  public void testIRealisticResults(){
    assertCodeCellRange(18, 24);
  }

  private void assertCodeCellRange(int start, int end){
    for(int n = start; n <= end; n++){
      Assertions.assertThat(ValueAtRisk.getNthCodeCell(n).runCell().outputHasErrors()).isFalse();
    }
  }


}

