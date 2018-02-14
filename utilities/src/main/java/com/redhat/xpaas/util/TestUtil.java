package com.redhat.xpaas.util;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Parameter;
import io.fabric8.openshift.client.ParameterValue;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class TestUtil {
  private final static Long TIMEOUT = RadConfiguration.timeout();
  private final static String webDriverName = RadConfiguration.webDriver();
  private final static String webDriverPath = RadConfiguration.webDriverPath();
  private final static Logger LOGGER = LoggerFactory.getLogger(TestUtil.class);

  public static ParameterValue[] processParameters(Map<String, String> parameters) {
    return parameters
      .entrySet()
      .stream()
      .map(entry -> new ParameterValue(entry.getKey(), entry
        .getValue())).collect(Collectors.toList())
      .toArray(new ParameterValue[parameters.size()]);
  }

  public static Parameter createParameter(String name, String value){
    Parameter p = new Parameter();
    p.setName(name);
    p.setValue(value);
    return p;
  }

  // Selenium utilities
  public static WebDriver createDriver(Boolean useHeadless){
    WebDriver webDriver;

    System.setProperty(webDriverName, webDriverPath);
    if(useHeadless){
      ChromeOptions options = new ChromeOptions();
      options.addArguments("headless");
      options.addArguments("--no-sandbox");
      webDriver = new ChromeDriver(options);
    }else {
      webDriver = new ChromeDriver();
    }
    return webDriver;
  }

  public static void waitFor(By element, WebDriver webDriver) {
    try {
      WaitUtil.waitFor(() -> elementPresent(element, webDriver), null, 1000L, RadConfiguration.httpTimeout());
    } catch (TimeoutException | InterruptedException e) {
      throw new IllegalStateException("Timeout exception during waiting for web element:" + e.getMessage());
    }
  }

  public static void waitFor(WebElement element, By byElement, WebDriver webDriver) {
    try {
      WaitUtil.waitFor(() -> relativeElementPresent(element, byElement, webDriver), null, 1000L, RadConfiguration.httpTimeout());
    } catch (TimeoutException | InterruptedException e) {
      throw new IllegalStateException("Timeout exception during waiting for web element:" + e.getMessage());
    }
  }

  public static WebElement waitForClickable(WebElement element, WebDriver webDriver) {
    (new WebDriverWait(webDriver, RadConfiguration.httpTimeout()))
      .until(ExpectedConditions.elementToBeClickable(element));

    return element;
  }

  public static WebElement moveToElementAndClick(WebElement element, WebDriver webDriver){
    Actions actions = new Actions(webDriver);
    actions.moveToElement(element).click().build().perform();
    return element;
  }

  // Wait for route to be exposed, and for it to reuturn status 200 before loading
  public static boolean pageLoaded(By byElement, Long interval, WebDriver webDriver){
    BooleanSupplier successCondition = () -> webDriver.findElements( byElement ).size() > 0;
    long timeout = System.currentTimeMillis() + RadConfiguration.httpTimeout();

    while (System.currentTimeMillis() < timeout) {
      webDriver.navigate().refresh();
      try {
        Thread.sleep(interval);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (successCondition.getAsBoolean()) {
        return true;
      }
    }
    return false;
  }

  public static boolean pageLoaded(Long interval, WebDriver webDriver, String url, BooleanSupplier successConditionForElementPresence){
    URL link = null;
    try {
      link = new URL(url);
    } catch (MalformedURLException e) {
      LOGGER.error(String.format("The following url provided is malformed: %s", url));
      e.printStackTrace();
    }

    // It may take some time for the router to return a status code of 200 if the
    // route is recently exposed, we wait for that here.
    URL finalLink = link;
    BooleanSupplier successConditionForConnection = () -> {
      try {

        HttpURLConnection connection = (HttpURLConnection) finalLink.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        return connection.getResponseCode() == 200;
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    };

    try {

      WaitUtil.waitFor(successConditionForConnection, null, 3000L, RadConfiguration.httpTimeout());
    } catch (InterruptedException | TimeoutException e) {
      e.printStackTrace();
      return false;
    }

    // Ensure that we wait for the element we expect to be loaded on page load
    // to infact load on the page.
    long timeout = System.currentTimeMillis() + RadConfiguration.httpTimeout();

    while (System.currentTimeMillis() < timeout) {
      webDriver.navigate().to(url);
      try {
        Thread.sleep(interval);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (successConditionForElementPresence.getAsBoolean()) {
        return true;
      }
    }
    return false;
  }

  private static boolean elementPresent(By byElement, WebDriver webDriver) {
    try {
      return webDriver.findElement(byElement).isDisplayed();
    } catch (java.util.NoSuchElementException | StaleElementReferenceException x) {
      return false;
    }
  }

  private static boolean relativeElementPresent(WebElement parent, By byChildElement, WebDriver webDriver) {
    try {
      WebElement element =  parent.findElement(byChildElement);
      return element.isDisplayed();
    } catch (java.util.NoSuchElementException | StaleElementReferenceException x) {
      return false;
    }
  }

}
