package com.redhat.xpaas.rad.MNIST.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class MNISTWebUI implements MNISTAPI {

  private final static Long TIMEOUT = RadConfiguration.timeout();
  private final String HOSTNAME;
  private final WebDriver webDriver;
  public static MNISTWebUI getInstance(String hostname) {
    return new MNISTWebUI(hostname);
  }

  private MNISTWebUI(String hostname) {
    this.HOSTNAME = hostname;
    webDriver = TestUtil.createDriver(RadConfiguration.useHeadlessForTests());
  }

  @Override
  public void loadPage(){
    String url = "http://" + HOSTNAME;
    webDriver.get(url);
    By canvas = By.id("main");
    BooleanSupplier successCondition = () -> webDriver.findElements(canvas).size() > 0;
    TestUtil.pageLoaded(2000L, webDriver, url, successCondition);
  }

  @Override
  public boolean drawThree(Long timeForResults, int fetchResultsAttempts) {
    By byCanvas = By.id("main");
    WebElement canvas = webDriver.findElement(byCanvas);
    Actions builder = new Actions(webDriver);
    Action drawAction = builder.moveToElement(canvas,150,80)  // start point
      .clickAndHold()
      .moveByOffset(120, 0) // second point
      .moveByOffset(0, 140)
      .moveByOffset(-120, 0)
      .moveByOffset(120, 0)
      .moveByOffset(0, 140)
      .moveByOffset(-120, 0)
      .click()
      .build();
    drawAction.perform();

    // Wait for results to load
    boolean loaded = false;
    int attemptsLeft = fetchResultsAttempts;
    while(!loaded && attemptsLeft != 0){
      String selector = "#output > tbody > tr:nth-child(2) > td:nth-child(2)";
      String output = webDriver.findElement(By.cssSelector(selector)).getText();
      if (output.isEmpty()){
        try {
          Thread.sleep(timeForResults);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } else {
        loaded = true;
      }
      attemptsLeft--;
    }
    return loaded;
  }

  @Override
  public Map<Integer, Double> modelResults(int modelType) {
    if(modelType > 2 || modelType < 1){
      return null;
    }
    Map<Integer, Double> numToResult = new HashMap<>();

    // Start from 1 to ignore the title row
    for(int i = 2 ; i < 12 ; i++){
      String selector = String.format("#output > tbody > tr:nth-child(%s) > td:nth-child(%s)", i, modelType + 1);
      WebElement output = webDriver.findElement(By.cssSelector(selector));
      numToResult.put(i - 2, Double.parseDouble(output.getText()));
    }

    return numToResult;
  }

  public void webDriverCleanup(){
    webDriver.quit();
  }

}
