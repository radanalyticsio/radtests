package com.redhat.xpaas.sparknotebook.entity;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.util.TestUtil;
import com.redhat.xpaas.wait.WaitUtil;
import org.openqa.selenium.*;

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
  public boolean outputHasErrors() {
    // If there are errors we expect an execution time to be displayed of the format
    // e.g. Took: 1.014s, at 2018-01-15 18:52

    String output = getOutput();
    return !Pattern.compile("Took:.*at").matcher(output).find();
  }

}
