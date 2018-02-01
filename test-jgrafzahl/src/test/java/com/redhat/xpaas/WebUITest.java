package com.redhat.xpaas;

import com.redhat.xpaas.logger.LogWrapper;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.jgrafzahl.api.JgrafZahlWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {
  LogWrapper log = new LogWrapper(Setup.class, "grafzahl");
  private static JgrafZahlWebUI jgrafZahl;
  private static JgrafZahlWebUI grafZahl;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private String NAMESPACE = RadConfiguration.masterNamespace();

  @Rule
  public TestRule watcher = log.getLogTestWatcher();

  @BeforeClass
  public static void setUP() {
    Setup setup = new Setup();
    JgrafZahlWebUI[] grafzahls = setup.initializeApplications();

    jgrafZahl = grafzahls[0];
    grafZahl = grafzahls[1];

    jgrafZahl.loadPage();
    grafZahl.loadPage();
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
    setup.cleanUp();
  }

  // OpenShift State tests
  @Test
  public void testAVerifyBuilds(){
    Assertions.assertThat(openshift.buildCompleted("app", "jgrafzahl")).isTrue();
    Assertions.assertThat(openshift.buildCompleted("app", "grafzahl")).isTrue();
  }

  @Test
  public void testBVerifyWordFountainBuild(){
    Assertions.assertThat(openshift.buildCompleted("app", "word-fountain")).isTrue();
  }

  @Test
  public void testCVerifyGrafZahlDeployments(){
    Assertions.assertThat(openshift.podRunning("app", "jgrafzahl")).isTrue();
    Assertions.assertThat(openshift.podRunning("app", "grafzahl")).isTrue();
  }

  @Test
  public void testDVerifyApacheKafkaDeployment(){
    Assertions.assertThat(openshift.podRunning("deploymentconfig", "apache-kafka")).isTrue();
  }

  @Test
  public void testEVerifyJgrafzahlWordFountainDeployment(){
    Assertions.assertThat(openshift.podRunning("app", "word-fountain")).isTrue();
  }

  // WebUI Tests
  @Test
  public void testFJgrafzahlWordCount(){
    Map<String, Integer> wordsToFrequency = null;
    try {
      wordsToFrequency = jgrafZahl.listenForWordFrequency(10);
    } catch (Exception e) {
      Assertions.fail("Could not successfully retrieve word count via HTTP GET");
      e.printStackTrace();
    }
    Assertions.assertThat(wordsToFrequency.size()).isEqualTo(10);
  }

  @Test
  public void testGJgrafzahlInputChange(){
    jgrafZahl.loadPage();
    int oldWordCount = jgrafZahl.getNumberOfWordsInInput();
    int newWordCount = oldWordCount + ThreadLocalRandom.current().nextInt(1, 6);
    Assertions.assertThat(jgrafZahl.changeNumberOfWords(newWordCount)).isTrue();
  }

  @Test
  public void testHGrafzahlWordCount(){
    Map<String, Integer> wordsToFrequency = null;
    try {
      wordsToFrequency = grafZahl.listenForWordFrequency(10);
    } catch (Exception e) {
      Assertions.fail("Could not successfully retrieve word count via HTTP GET");
      e.printStackTrace();
    }
    Assertions.assertThat(wordsToFrequency.size()).isEqualTo(10);
  }

  @Test
  public void testIGrafzahlInputChange(){
    grafZahl.loadPage();
    int oldWordCount = grafZahl.getNumberOfWordsInInput();
    int newWordCount = oldWordCount + ThreadLocalRandom.current().nextInt(1, 6);
    Assertions.assertThat(grafZahl.changeNumberOfWords(newWordCount)).isTrue();
  }



}

