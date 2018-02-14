package com.redhat.xpaas.sparknotebook;

import com.redhat.xpaas.sparknotebook.entity.CodeCell;
import com.redhat.xpaas.sparknotebook.entity.CodeCellWeb;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class SparkNotebookWebUI implements SparkNotebookAPI {
  private final WebDriver webDriver;
  private final String HOSTNAME;

  public SparkNotebookWebUI(WebDriver webDriver, String hostname){
    this.webDriver = webDriver;
    this.HOSTNAME = hostname;
  }

  @Override
  public void login(String password) {}

  @Override
  public void loadProjectByURL(String projectName) {
    String url = "http://" + HOSTNAME + "/notebooks/" + projectName;
    webDriver.get(url);
    By byMainPage = By.id("notebook-container");
    BooleanSupplier successCondition = () -> webDriver.findElements(byMainPage).size() > 0;
    TestUtil.pageLoaded(2000L, webDriver, url, successCondition);
  }

  @Override
  public CodeCell getNthCodeCell(int n) {
    if (n <= 0){
      return null;
    }
    List<WebElement> codeCells = getAllCodeCells();

    return codeCells.isEmpty() ? null : new CodeCellWeb(codeCells.get(n - 1), webDriver);
  }

  @Override
  public void webDriverCleanup() {
    webDriver.quit();
  }

  private List<WebElement> getAllCodeCells(){
    By byNotebookContainer = By.cssSelector("#notebook-container > div");
    WebDriverWait wait = new WebDriverWait(webDriver,30);
    wait.until(ExpectedConditions.visibilityOfElementLocated(byNotebookContainer));

    List<WebElement> container = webDriver.findElements(byNotebookContainer);
    return container.stream().filter(cells -> cells.getAttribute("class").contains("code_cell")).collect(Collectors.toList());
  }

}
