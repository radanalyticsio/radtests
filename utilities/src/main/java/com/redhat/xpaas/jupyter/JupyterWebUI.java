package com.redhat.xpaas.jupyter;

import com.redhat.xpaas.jupyter.entity.CodeCell;
import com.redhat.xpaas.jupyter.entity.CodeCellWeb;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class JupyterWebUI implements JupyterAPI{
  private final WebDriver webDriver;
  private final String HOSTNAME;

  public JupyterWebUI(WebDriver webDriver, String hostname){
    this.webDriver = webDriver;
    this.HOSTNAME = hostname;
  }

  @Override
  public void login(String password) {
    String url = "http://" + HOSTNAME + "/login";
    webDriver.get(url);
    By byPassword =  By.id("password_input");
    By byLogin = By.id("login_submit");
    BooleanSupplier successCondition = () -> webDriver.findElements(byPassword).size() > 0;
    TestUtil.pageLoaded(2000L, webDriver, url, successCondition);

    WebDriverWait wait = new WebDriverWait(webDriver,30);
    wait.until(ExpectedConditions.visibilityOfElementLocated(byPassword));
    wait.until(ExpectedConditions.visibilityOfElementLocated(byLogin));

    WebElement passwordInput = webDriver.findElement(byPassword);
    passwordInput.sendKeys(password);
    WebElement login = webDriver.findElement(byLogin);
    login.click();
  }

  @Override
  public void loadProject(String projectName) {
    String url = "http://" + HOSTNAME;
    webDriver.get(url);
    By byNoteBookList = By.cssSelector("#notebook_list");

    // Wait for projects list to load
    WebDriverWait wait = new WebDriverWait(webDriver,30);
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#notebook_list > div:nth-child(2)")));

    WebElement noteBookList = webDriver.findElement(byNoteBookList);
    List<WebElement> projects = noteBookList.findElements(By.xpath("//*[@id='notebook_list']/div[@class='list_item row']"));

    for(WebElement project : projects ){
      WebElement item = project.findElement(By.xpath("./div/a"));
      if(item.getText().equals(projectName)){
        item.click();
      }
    }
  }

  @Override
  public void loadProjectByURL(String projectName) {
    String url = "http://" + HOSTNAME + "/notebooks/" + projectName;
    webDriver.get(url);
    By byMainPage = By.id("ipython-main-app");
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
