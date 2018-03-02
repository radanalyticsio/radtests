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
    assertCodeCell(1);
  }

  @Test
  public void testCLoadingData(){
    assertCodeCell(2);
  }

  @Test
  public void testDCalculatingHistoricalReturns(){
    assertCodeCell(3);
  }

  @Test
  public void testEExpectedReturnDist(){
    assertCodeCellRange(4, 5);
  }

  @Test
  public void testFGetSecurityPrices(){
    assertCodeCell(6);
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

  private void assertCodeCell(int cellIndex){
    assertCodeCellRange(cellIndex, cellIndex);
  }

  private void assertCodeCellRange(int start, int end){
    boolean outputHasErrors;
    for(int n = start; n <= end; n++){
      try {
        outputHasErrors = ValueAtRisk.getNthCodeCell(n).runCell().outputHasErrors();
        Assertions.assertThat(outputHasErrors).as("Check output status of cell %s", n).isFalse();
      } catch (AssertionError e) {
        Assertions.assertThat(e).hasMessage(String.format("Expected:<false> but was <%s>. With outputmessage: %s", false,ValueAtRisk.getNthCodeCell(n).runCell().getOutput()));
      }
    }
  }
}

