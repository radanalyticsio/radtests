package com.redhat.xpaas.sparknotebook.entity;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.util.TestUtil;
import com.redhat.xpaas.wait.WaitUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

public class CodeCellWeb implements CodeCell {
  private final WebElement CELL;
  private final WebDriver webDriver;

  public CodeCellWeb(WebElement cell, WebDriver webDriver){
    this.CELL = cell;
    this.webDriver = webDriver;
  }

  @Override
  public char getInputPrompt() {
    return CELL.findElement(By.className("input_prompt")).getText().charAt(4);
  }

  @Override
  public CodeCell runCell() {
    By byRunButton = By.cssSelector("div.cell-context-buttons > div > a:nth-child(1)");
    By byExpandSubDropDownMenu = By.cssSelector("div.cell-context-buttons > div > a.btn.dropdown-toggle");
    By bySubMenuClear = By.cssSelector("li[data-menu-command='clear_current_output']");
    By bySubDropDownMenu = By.xpath("./div[7]/div/ul");

    // Bring Cell's visibility into viewport
    JavascriptExecutor jse = (JavascriptExecutor)webDriver;
    jse.executeScript("arguments[0].scrollIntoView(true);", CELL);
    jse.executeScript("window.scrollBy(0,-200);");

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // Make context menu for this code cell visible
    TestUtil.waitFor(CELL, byExpandSubDropDownMenu, webDriver);
    CELL.findElement(byExpandSubDropDownMenu).click();

//    BooleanSupplier subDropDownMenuDisplayed = () -> {
//      CELL.findElement(byExpandSubDropDownMenu).click();
//      return relativeElementPresent(CELL, bySubDropDownMenu, webDriver);
//    };
//
//    try {
//      WaitUtil.waitFor(subDropDownMenuDisplayed, null, 500, RadConfiguration.httpTimeout());
//    } catch (InterruptedException | TimeoutException e) {
//      e.printStackTrace();
//    }

    // Clear any output that may exist
    TestUtil.waitFor(CELL, bySubDropDownMenu, webDriver);
    WebElement dropDownSubMenu = CELL.findElement(bySubDropDownMenu);
    dropDownSubMenu.findElement(bySubMenuClear).click();

    // Execute code
    CELL.findElement(byRunButton).click();

    // Execution is complete when progress bar's cancel button goes hidden
    BooleanSupplier successCondition = () -> {
      WebElement cancellButon = CELL.findElement(By.cssSelector("div.progress-bar > a.cancel-cell-btn"));
      return cancellButon.getAttribute("style").equals("display: none;");
    };

    try {
      WaitUtil.waitFor(successCondition, null, 3000L, RadConfiguration.httpTimeout());
    } catch (InterruptedException|TimeoutException e) {
      e.printStackTrace();
    }

    return this;
  }

  @Override
  public String getOutput() {

    List<WebElement> outputAreas = CELL.findElements(By.className("output_area"));

    StringBuilder output = new StringBuilder();
    for(WebElement element : outputAreas){
      By byOutput = By.xpath("./div[2]/pre");
      By byTimeOutput = By.cssSelector("small");
      if(element.findElements(byOutput).size() != 0){
        output.append(element.findElement(byOutput).getText()).append("\n");
      }
      if(element.findElements(byTimeOutput).size() != 0){
        output.append(element.findElement(byTimeOutput).getText()).append("\n");
      }
    }

    return output.toString();
  }

  @Override
  public String getOutput(Long timeout) {

    By byOutputArea = By.className("output");

    BooleanSupplier successCondition = () ->
      !CELL.findElement(byOutputArea).getAttribute("style")
        .contains("display: none;");

    try {
      WaitUtil.waitFor(successCondition, null, 1000L, timeout);
    } catch (InterruptedException|TimeoutException e) {
      e.printStackTrace();
    }

    return getOutput();
  }

  @Override
  public boolean outputHasErrors() {
    // If there are errors we expect an execution time to be displayed of the format
    // e.g. Took: 1.014s, at 2018-01-15 18:52

    String output = getOutput();
    return !Pattern.compile("Took:.*at").matcher(output).find();
  }

  /**
   * Return WebElement representation of code line at line number starting from 1.
   */
  @Override
  public WebElement getCodeLine(int lineNumber){
    List<WebElement> codeLines = CELL.findElements(By.className("CodeMirror-line"));
    return codeLines.get(lineNumber - 1);
  }

  @Override
  public boolean setCodeLine(WebElement element){
    // Todo: Implement
    return false;
  }

  /**
   * Find and replace a line of code in this code cell. Return true upon success.
   */
  @Override
  public boolean findAndReplaceInCell(String find, String replace){
    By byEditMenu = By.cssSelector("#menus > div > div > ul > li:nth-child(2) > a");
    By byFindAndReplace = By.cssSelector("#find_and_replace > a");
    By byFindAndReplaceModal = By.cssSelector("#find-and-replace");
    By byFindInputField = By.cssSelector("#find-and-replace > div:nth-child(1) > div > input");
    By byReplaceInputField = By.cssSelector("#find-and-replace > div:nth-child(2) > input");
    By byFormSubmitButton = By.cssSelector("body > div.modal.fade.in > div > div > div.modal-footer > button");

    // Set search to this cell's scope
    CELL.click();
    try {
      webDriver.findElement(byEditMenu).click();
      webDriver.findElement(byFindAndReplace).click();

      WebDriverWait wait = new WebDriverWait(webDriver,30);
      wait.until(ExpectedConditions.visibilityOfElementLocated(byFindAndReplaceModal));

      WebElement findInputField = webDriver.findElement(byFindInputField);
      WebElement replaceInputField = webDriver.findElement(byReplaceInputField);

      findInputField.sendKeys(find);
      replaceInputField.sendKeys(replace);

      WebElement formSubmitButton = webDriver.findElement(byFormSubmitButton);
      formSubmitButton.click();

      wait.until(ExpectedConditions.invisibilityOfElementLocated(byFindAndReplaceModal));
    } catch (Exception e){
      return false;
    }

    return true;
  }



  private static boolean relativeElementPresent(WebElement element, By byElement, WebDriver webDriver) {
    try {
      return element.findElement(byElement).isDisplayed();
    } catch (java.util.NoSuchElementException | StaleElementReferenceException x) {
      return false;
    }
  }
}
