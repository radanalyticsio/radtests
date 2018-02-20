package com.redhat.xpaas.rad.jgrafzahl.api;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.util.TestUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.BooleanSupplier;

public class JgrafZahlWebUI implements JgrafZahlAPI{

  private final String HOSTNAME;
  private final WebDriver webDriver;

  public static JgrafZahlWebUI getInstance(String hostname) {
    return new JgrafZahlWebUI(hostname);
  }

  private JgrafZahlWebUI(String hostname) {
    this.HOSTNAME = hostname;
    webDriver = TestUtil.createDriver(RadConfiguration.useHeadlessForTests());
  }

  @Override
  public boolean loadPage(){
    String url = "http://" + HOSTNAME;
    webDriver.get(url);
    By byFirstBar = By.className("c3-event-rect-0");
    By bySecondBar = By.className("c3-event-rect-1");
    BooleanSupplier successCondition = () -> webDriver.findElements(byFirstBar).size() > 0 && webDriver.findElements(bySecondBar).size() > 0;
    return TestUtil.pageLoaded(2000L, webDriver, url, successCondition);
  }

  @Override
  public Map<String, Integer> getWordFrequency(){
    Map<String, Integer> wordsToFrequency = new HashMap<>();
    By byXAxis = By.xpath("//*[@class='c3-axis c3-axis-x']");
    WebElement xAxis = webDriver.findElement(byXAxis);
    List<WebElement> childElements = xAxis.findElements(By.xpath(".//*"));

    int barIndex = 0;
    String barClass;
    String word;
    By byBar;
    By byTooltip;
    WebElement bar;
    WebElement tooltip;
    String tooltipValue;
    for (WebElement e : childElements ){
      if (e.getTagName().equals("tspan")){
        word = e.getText();
        barClass = "c3-event-rect-" + barIndex;
        byBar = By.className(barClass);
        TestUtil.waitFor(byBar, webDriver);
        bar = webDriver.findElement(byBar);

        Actions builder = new Actions(webDriver);
        builder.moveToElement(bar).build().perform();

        byTooltip = By.xpath("//*[@id=\"bar-chart\"]/div/table/tbody/tr/td[2]");
        TestUtil.waitFor(byTooltip, webDriver);
        tooltip = webDriver.findElement(byTooltip);
        tooltipValue = tooltip.getText();

        wordsToFrequency.put(word, (tooltipValue.isEmpty()) ? -1 : Integer.parseInt(tooltipValue));
        barIndex++;
      }
    }

    return wordsToFrequency;
  }

  @Override
  public int getNumberOfWordsInInput() {
    By input = By.id("nterms");
    TestUtil.waitFor(input, webDriver);
    return Integer.parseInt(webDriver.findElement(input).getAttribute("value"));
  }

  @Override
  public int getNumberOfWordsInXAxis() {
    List<WebElement> elements = webDriver.findElements(By.cssSelector("#bar-chart > svg > g:nth-child(2) > g.c3-axis.c3-axis-x > g"));
    return elements.size() ;
  }

  @Override
  public boolean changeNumberOfWords(int newWordCount) {
    int oldWordCount = getNumberOfWordsInInput();
    if (oldWordCount == newWordCount) {
      return true;
    }

    By byInput = By.id("nterms");
    TestUtil.waitFor(byInput, webDriver);
    WebElement input = webDriver.findElement(byInput);
    input.clear();
    input.sendKeys(String.valueOf(newWordCount));

    // Wait for new word count to be loaded, attempt for 2 min
    int maxNumberOfAttempts = 24;
    int currentWordCount = getNumberOfWordsInXAxis();
    while(currentWordCount != newWordCount && maxNumberOfAttempts != 0){
      try {
        Thread.sleep(5000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      currentWordCount = getNumberOfWordsInXAxis();
      maxNumberOfAttempts--;
    }
    return currentWordCount == newWordCount;
  }

  @Override
  public void webDriverCleanup(){
    webDriver.quit();
  }

  @Override
  public Map<String, Integer> listenForWordFrequency(int wordCount) throws Exception {

    StringBuilder result = new StringBuilder();
    URL url = new URL("http://" + HOSTNAME + "/data?n=" + wordCount);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line;
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    rd.close();
    return JSONParser(result.toString());
  }

  private Map<String, Integer> JSONParser(String json){
    JSONObject obj = new JSONObject(json);
    JSONArray categories = obj.getJSONArray("categories");
    JSONArray data = obj.getJSONArray("data").getJSONArray(0);
    data.remove(0); // First index contains irrelevant "counts" string value.
    Map<String, Integer> wordsToFrequency = new HashMap<>();

    if(categories.length() != data.length()){
      return wordsToFrequency;
    }

    for (int i = 0; i < categories.length(); i++) {
      wordsToFrequency.put(categories.getString(i), data.getInt(i));
    }

    return wordsToFrequency;
  }

}
