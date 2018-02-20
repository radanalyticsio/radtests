package com.redhat.xpaas;

import com.redhat.xpaas.logger.Loggable;
import com.redhat.xpaas.rad.ophicleide.api.OphicleideWebUI;
import com.redhat.xpaas.rad.ophicleide.api.entity.QueryResults;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

@Loggable(project = "ophicleide")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static OphicleideWebUI ophicleide;
  private static final String modelName = RadConfiguration.modelName();
  private static final String modelUrls = RadConfiguration.modelURL();
  private static final String queryWord = RadConfiguration.queryWord();

  @BeforeClass
  public static void setUP() throws TimeoutException, InterruptedException {
    Setup setup = new Setup();
    WebUITest.ophicleide = setup.initializeApplications();
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
    setup.cleanUp();
  }

  @Test
  public void testATraining() {
    String status = ophicleide.trainModel(modelName, modelUrls);
    Assertions.assertThat(status).isEqualTo("Status: ready");
  }

  @Test
  public void testBQuery() {
    QueryResults result = ophicleide.createQuery(modelName, queryWord);
    Assertions.assertThat(result.getResult().size()).isEqualTo(5);
  }

  @Test
  public void testCDeletion() {
    Boolean result = ophicleide.deleteModel(modelName);
    Assertions.assertThat(result).isTrue();
  }

}

