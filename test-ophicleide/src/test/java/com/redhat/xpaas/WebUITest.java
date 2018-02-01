package com.redhat.xpaas;

import com.redhat.xpaas.logger.LogWrapper;
import com.redhat.xpaas.rad.ophicleide.api.OphicleideWebUI;
import com.redhat.xpaas.rad.ophicleide.api.entity.QueryResults;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  LogWrapper log = new LogWrapper(Setup.class, "ophicleide");
  private static OphicleideWebUI ophicleide;
  private static final String modelName = RadConfiguration.modelName();
  private static final String modelUrls = RadConfiguration.modelURL();
  private static final String queryWord = RadConfiguration.queryWord();

  @Rule
  public TestRule watcher = log.getLogTestWatcher();

  @BeforeClass
  public static void setUP() {
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

