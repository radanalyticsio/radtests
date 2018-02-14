package com.redhat.xpaas.jupyter.entity;

import com.redhat.xpaas.wait.WaitUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

public class CodeCellWeb implements CodeCell {
  private final WebElement CELL;
  private final WebDriver webDriver;
  private int executionCount;

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
    CELL.click();
    By byRunButton = By.cssSelector("#run_int > button:nth-child(1)");
    webDriver.findElement(byRunButton).click();

    // Input prompt updates to next execution count after code finishes executing
    BooleanSupplier successCondition = () -> {
      char inputPrompt = getInputPrompt();
      if(Character.isDigit(inputPrompt)){
        int newExecutionCount = inputPrompt - '0';
        if(newExecutionCount > this.executionCount){
          this.executionCount = newExecutionCount;
          return true;
        }
        return false;
      } else if (inputPrompt == ' ' || inputPrompt == '*') {
        return false;
      } else{
        throw new RuntimeException("Input Prompt in an unknown state after Cell execution.");
      }
    };

    try {
      WaitUtil.waitFor(successCondition);
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
      if(element.findElements(byOutput).size() != 0){
        output.append(element.findElement(byOutput).getText()).append("\n");
      }
    }

    return output.toString();
  }

  @Override
  public String getOutput(Long timeout) {
    if(executionCount < 1) {
      throw new RuntimeException("Code cell must be executed at least once to retrieve output.");
    }

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
    if(executionCount < 1) {
      throw new RuntimeException("Code cell must be executed at least once to retrieve output.");
    }

    String output = getOutput();
    return output.contains("Traceback (most recent call last)");
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

}
