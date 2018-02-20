package com.redhat.xpaas.rad.ophicleide.api;

import com.redhat.xpaas.rad.ophicleide.api.entity.QueryResults;
import com.redhat.xpaas.rad.ophicleide.api.entity.Result;
import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.util.TestUtil;
import com.redhat.xpaas.wait.WaitUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

public class OphicleideWebUI implements OphicleideAPI{
  private final String HOSTNAME;
  private final WebDriver webDriver;
  private final static Long TIMEOUT = RadConfiguration.timeout();

  public static OphicleideWebUI getInstance(String hostname) {
    return new OphicleideWebUI(hostname);
  }

  private OphicleideWebUI(String hostname){
    this.HOSTNAME = hostname;
    webDriver = TestUtil.createDriver(RadConfiguration.useHeadlessForTests());
  }

  @Override
  public String trainModel(String name, String urls) {
    String url = "http://" + HOSTNAME;
    webDriver.get(url);

    WebDriverWait wait = new WebDriverWait(webDriver, TIMEOUT);
    BooleanSupplier successCondition = () -> webDriver.findElements(By.xpath("//button[contains(@title,'Train Model')]") ).size() > 0;

    // Ensure that page is loaded
    if(!TestUtil.pageLoaded(2000L, webDriver, url, successCondition)){
      return "";
    }

    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@title,'Train Model')]")));
    webDriver.findElement(By.xpath("//button[contains(@title,'Train Model')]")).click();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("modal-title")));
    WebElement modelName = webDriver.findElement(By.id("modelName"));
    WebElement modelUrls = webDriver.findElement(By.id("modelUrls"));

    modelName.sendKeys(name);
    modelUrls.sendKeys(urls);

    webDriver.findElement(By.xpath("//button[contains(text(),'Train')]")).click();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(),'Status: ready')]")));

    return webDriver.findElement(By.xpath("//div[contains(text(),'Status: ready')]")).getText();
  }

  @Override
  public boolean deleteModel(String modelName) {
    String url = "http://" + HOSTNAME;
    webDriver.get(url);
    By delete = By.xpath("//button[contains(@title,'Delete') and contains(@class, 'btn-default')]");
    By deleteFinal = By.xpath("//button[contains(text(),'Delete') and contains(@class, 'btn-danger')]");
    By model = By.xpath("//div[contains(text(),'Status: ready')]");

    // Ensure model exists on page
    waitFor(model);
    waitFor(delete);
    List<WebElement> deleteLinks = webDriver.findElements(model);

    // Model not present
    if (deleteLinks.isEmpty()) return false;

    webDriver.findElement(delete).click();
    waitFor(deleteFinal);
    webDriver.findElement(deleteFinal).click();
    waitForDeletion(model);

    return webDriver.findElements(model).isEmpty();
  }

  // FIXME: Currently retrieves only the first query that was made
  @Override
  public QueryResults createQuery(String modelName, String modleQuery) {
    QueryResults results = new QueryResults(modelName);

    By createQuery = By.xpath("//button[contains(@title,'Create Query')]");
    By inputField = By.id("queryWord");
    By performQuery = By.xpath("//button[contains(text(),'Query')]");
    By result = By.xpath("//*[@class='card-pf-info text-center']");
    String url = "http://" + HOSTNAME + "/#/queries";
    webDriver.get(url);
    waitFor(createQuery);

    // Ensure that page loaded
    webDriver.findElement(createQuery).click();
    waitFor(inputField);
    WebElement input = webDriver.findElement(inputField);
    input.sendKeys(modleQuery);
    webDriver.findElement(performQuery).click();

    waitFor(result);

    // Retrieve results and parse strings into proper form
    WebElement resultsData = webDriver.findElements(result).get(0);
    String[] list = resultsData.getText().split("\\s+");

    for (int i = 1; i < list.length; i+=2){
      Result r = new Result<>(list[i], Double.parseDouble(list[i+1].substring(1, list[i+1].length() - 1)));
      results.addResult(r);
    }

    return results;
  }

  public void webDriverCleanup(){
    webDriver.quit();
  }

  private void waitFor(By element) {
    try {
      WaitUtil.waitFor(() -> elementPresent(element), null, 1000L, TIMEOUT);
    } catch (TimeoutException | InterruptedException e) {
      throw new IllegalStateException("Timeout exception during waiting for web element:" + e.getMessage());
    }
  }

  private void waitForDeletion(By element) {
    try {
      WaitUtil.waitFor(() -> elementNotPresent(element), null, 1000L, TIMEOUT);
    } catch (TimeoutException | InterruptedException e) {
      throw new IllegalStateException("Timeout exception during waiting for web element:" + e.getMessage());
    }
  }

  private boolean elementPresent(By element) {
    try {
      return webDriver.findElement(element).isDisplayed();
    } catch (NoSuchElementException | StaleElementReferenceException x) {
      return false;
    }
  }

  private boolean elementNotPresent(By element) {
    List<WebElement> elementList = webDriver.findElements(element);
    try {
      return elementList.isEmpty();
    } catch (NoSuchElementException | StaleElementReferenceException x) {
      return false;
    }
  }
}



