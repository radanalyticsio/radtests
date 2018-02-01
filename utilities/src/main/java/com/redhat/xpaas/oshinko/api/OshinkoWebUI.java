package com.redhat.xpaas.oshinko.api;

import com.redhat.xpaas.oshinko.api.entity.SparkCluster;
import com.redhat.xpaas.oshinko.api.entity.SparkPod;
import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.util.TestUtil;
import com.redhat.xpaas.wait.WaitUtil;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

public class OshinkoWebUI implements OshinkoAPI {
  private final static Long TIMEOUT = RadConfiguration.timeout();
  private final String hostname;
  private final WebDriver webDriver;
  public static OshinkoWebUI getInstance(String hostname) {
    return new OshinkoWebUI(hostname);
  }

  private OshinkoWebUI(String hostname) {
    this.hostname = hostname;
    webDriver = TestUtil.createDriver(RadConfiguration.useHeadlessForOshinko());
  }

  @Override
  public boolean createCluster(String clusterName) {
    return createCluster(clusterName, -10);
  }

  @Override
  public boolean createCluster(String clusterName, int workersCount) {
    return createCluster(clusterName, workersCount, -10, null, null, null, null);
  }

  @Override
  public boolean createCluster(String clusterName, int workersCount, int mastersCount, String masterConfig, String workerConfig, String storedConfig, String sparkImage) {
    String url = "http://" + hostname + "/webui/#/clusters";
    By deployButton = By.id("startbutton");
    By blankSlate = By.cssSelector("div[class*='well blank-slate-pf spacious ng-scope']");
    By clusterNameField = By.id("cluster-new-name");
    By clusterWorkersCountField = By.id("cluster-new-workers");
    By createButton = By.id("createbutton");
    webDriver.navigate().to(url);

    BooleanSupplier successCondition = () -> webDriver.findElements( deployButton ).size() > 0 && webDriver.findElements( blankSlate ).size() > 0;

    // Refresh until page loads all elements
    Boolean pageLoaded = false;
    pageLoaded = TestUtil.pageLoaded(2000L, webDriver, url, successCondition);
    if(!pageLoaded){
      return false;
    }

    waitFor(deployButton);
    webDriver.findElement(deployButton).click();
    waitFor(clusterNameField);
    waitFor(clusterWorkersCountField);
    waitFor(createButton);

    webDriver.findElement(clusterNameField).sendKeys(clusterName);
    if(workersCount >= 0) {
      webDriver.findElement(clusterWorkersCountField).clear();
      webDriver.findElement(clusterWorkersCountField).sendKeys(Integer.toString(workersCount));
    }
    webDriver.findElement(createButton).click();

    return true;
  }

  @Override
  public SparkCluster getCluster(String clusterName) {
    String url = "http://" + hostname + "/#/clusters/" + clusterName;

    webDriver.navigate().to(url);

    By nonExistingCluster = By.xpath("//div[@class='well blank-slate-pf spacious ng-scope']/h3");
    if (webDriver.findElements(nonExistingCluster).size() > 0) {
      return null;
    }

    By name = By.xpath("//dl[@class='dl-horizontal']/dt[. = 'Name']/following-sibling::dd");
    By status = By.xpath("//dl[@class='dl-horizontal']/dt[. = 'Status']/following-sibling::dd");
    By master = By.xpath("//dl[@class='dl-horizontal']/dt[. = 'Master']/following-sibling::dd");
    By workerCount = By.xpath("//dl[@class='dl-horizontal']/dt[. = 'Worker count']/following-sibling::dd");
    By podsTable = By.xpath("//tbody[@class='ng-scope']");

    SparkCluster cluster = new SparkCluster();

    cluster.setClusterName(webDriver.findElement(name).getText());
    cluster.setStatus(webDriver.findElement(status).getText());
    cluster.setMasterUrl(webDriver.findElement(master).getText());
    cluster.setWorkersCount(Integer.parseInt(webDriver.findElement(workerCount).getText()));

    List<SparkPod> sparkPods = new ArrayList<>();

    webDriver.findElements(podsTable).forEach( x -> {
      String[] splittedRow = x.getText().split("\\n");
      SparkPod pod = new SparkPod();

      pod.setIp(splittedRow[0]);
      pod.setType(splittedRow[1]);
      // TODO here should be status, however its not showed up in podded web ui

      sparkPods.add(pod);
    });
    cluster.setSparkPods(sparkPods);
    cluster.setMastersCount(((int) sparkPods.stream().filter(x -> x.getType().equals("master")).count()));

    return cluster;
  }

  @Override
  public List<SparkCluster> listClusters() {
    String url = "http://" + hostname + "/#/clusters";

    By clustersTable = By.xpath("//tbody[@class='ng-scope']");
    List<String> clusterNames = new ArrayList<>();

    webDriver.navigate().to(url);
    webDriver.findElements(clustersTable).forEach( x -> clusterNames.add(x.getText().split(" ")[0]));

    List<SparkCluster> clusters = new ArrayList<>();
    clusterNames.forEach( name -> clusters.add(getCluster(name)));

    return clusters;
  }

  @Override
  public boolean scaleCluster(String clusterName, int workersCount) {
    String url = "http://" + hostname + "/#/clusters/" + clusterName;

    By actionsButton = By.xpath("//button[contains(text(),'Actions')]");
    By scaleAction = By.xpath("//a[contains(text(),'Scale')]");
    By workersCountField = By.name("numworkers");
    By scaleButton = By.id("scalebutton");

    webDriver.navigate().to(url);
    waitFor(actionsButton);

    webDriver.findElement(actionsButton).click();
    waitFor(scaleAction);

    webDriver.findElement(scaleAction).click();
    waitFor(workersCountField);
    waitFor(scaleButton);

    webDriver.findElement(workersCountField).clear();
    webDriver.findElement(workersCountField).sendKeys(Integer.toString(workersCount));
    webDriver.findElement(scaleButton).click();

    return true;
  }

  @Override
  public boolean deleteCluster(String clusterName) {
    String url = "http://" + hostname + "/#/clusters/" + clusterName;

    By actionsButton = By.xpath("//button[contains(text(),'Actions')]");
    By deleteAction = By.xpath("//a[contains(text(),'Delete')]");
    By deleteButton = By.id("deletebutton");

    webDriver.navigate().to(url);
    waitFor(actionsButton);

    webDriver.findElement(actionsButton).click();
    waitFor(deleteAction);

    webDriver.findElement(deleteAction).click();
    waitFor(deleteButton);

    webDriver.findElement(deleteButton).click();

    return true;
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

  private boolean elementPresent(By element) {
    try {
      return webDriver.findElement(element).isDisplayed();
    } catch (NoSuchElementException | StaleElementReferenceException x) {
      return false;
    }
  }
}
