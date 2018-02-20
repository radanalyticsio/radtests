package com.redhat.xpaas.rad.jgrafzahl.api;

import java.util.Map;

public interface JgrafZahlAPI {

  boolean loadPage();

  Map<String, Integer> getWordFrequency();

  int getNumberOfWordsInInput();

  boolean changeNumberOfWords(int newWordCount);

  int getNumberOfWordsInXAxis();

  void webDriverCleanup();

  Map<String, Integer> listenForWordFrequency(int wordCount) throws Exception;

}
