package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.BlockChainAnalysis.api.BlockChainAnalysisSparkWebUI;
import com.redhat.xpaas.rad.BlockChainAnalysis.api.BlockChainAnalysisWebUI;
import com.redhat.xpaas.sparknotebook.entity.CodeCell;
import com.redhat.xpaas.util.Tuple;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

@Loggable(project ="blockchain")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static BlockChainAnalysisWebUI BlockChainAnalysis;
  private static BlockChainAnalysisSparkWebUI BlockChainAnalysisSpark;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  @BeforeClass
  public static void setUP() throws TimeoutException, InterruptedException {
    Setup setup = new Setup();
    Tuple<BlockChainAnalysisWebUI, BlockChainAnalysisSparkWebUI> blockchainNoteBooks = setup.initializeApplications();
    BlockChainAnalysis = blockchainNoteBooks.getFirst();
    BlockChainAnalysisSpark = blockchainNoteBooks.getSecond();

    BlockChainAnalysis.loadProjectByURL("blockchain.ipynb");
    BlockChainAnalysisSpark.loadProjectByURL("blockchain.snb.ipynb");
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
    setup.cleanUp();
  }

  @Test
  public void testAVerifyDeployment(){
    Assertions.assertThat(openshift.podRunning("app", "bitcoin-notebook")).isTrue();
    Assertions.assertThat(openshift.podRunning("app", "bitcoin-spark-notebook")).isTrue();

  }

  @Test
  public void testBJupyterNotebookVideoDisplay(){
    assertCodeCell(1);
  }

  @Test
  public void testCJupyterNotebookBasicSetup(){
    assertCodeCell(2);
  }

  @Test
  public void testDJupyterNotebookLoadingData(){
    assertCodeCellRange(3, 6);
  }

  @Test
  public void testEJupyterNotebookDataCleansing(){
    assertCodeCell(7);
  }

  @Test
  public void testFJupyterNotebookConstructingTheGraph(){
    assertCodeCellRange(8, 21);
  }

  @Test
  public void testGJupyterNotebookVisualizationOfSubGraph(){
    assertCodeCellRange(22, 28);
  }

  @Test
  public void testHSparkNotebookBasicSetup(){
    assertCodeCellSpark(1);
  }

  @Test
  public void testISparkNotebookCheckDataOnDisk(){
    assertCodeCellRangeSpark(2, 3);
  }

  @Test
  public void testJSparkNotebookLoadTheData(){
    assertCodeCellSpark(4);
  }

  @Test
  public void testKSparkNotebookNumberOfVertices(){
    assertCodeCellSpark(5);
  }

  @Test
  public void testLSparkNotebookCleanTheData(){
    assertCodeCellRangeSpark(6, 7);

  }

  @Test
  public void testMSparkNotebookNumberOfEdges(){
    assertCodeCellSpark(8);
  }

  @Test
  public void testNSparkNotebookCreatingTheGraph(){
    assertCodeCellRangeSpark(9, 10);
  }

  @Test
  public void testOSparkNotebookCalculatingThePageRank(){
    assertCodeCellRangeSpark(11, 13);
  }

  @Test
  public void testPSparkNotebookHelperFunctions(){
    assertCodeCellRangeSpark(14, 17);
  }

  private void assertCodeCell(int cellIndex){
    assertCodeCellRange(cellIndex, cellIndex);
  }

  private void assertCodeCellRange(int start, int end){
    boolean outputHasErrors;
    com.redhat.xpaas.jupyter.entity.CodeCell cell;
    for(int n = start; n <= end; n++){
      cell = BlockChainAnalysis.getNthCodeCell(n);
      try {
        outputHasErrors = cell.runCell().outputHasErrors();
        Assertions.assertThat(outputHasErrors).as("Check output status of cell %s", n).isFalse();
      } catch (AssertionError e) {
        Assertions.assertThat(e).hasMessage(String.format("Expected:<false> but was <%s>. With outputmessage: %s",
          true, cell.getOutput()));
      }
    }
  }

  private void assertCodeCellSpark(int cellIndex){
    assertCodeCellRangeSpark(cellIndex, cellIndex);
  }

  private void assertCodeCellRangeSpark(int start, int end){
    boolean outputHasErrors;
    CodeCell cell;
    for(int n = start; n <= end; n++){
      cell = BlockChainAnalysisSpark.getNthCodeCell(n);
      try {
        outputHasErrors = cell.runCell().outputHasErrors();
        Assertions.assertThat(outputHasErrors).as("Check output status of cell %s", n).isFalse();
      } catch (AssertionError e) {
        Assertions.assertThat(e).hasMessage(String.format("Expected:<false> but was <%s>. With outputmessage: %s",
          true, cell.getOutput()));
      }
    }
  }
}

