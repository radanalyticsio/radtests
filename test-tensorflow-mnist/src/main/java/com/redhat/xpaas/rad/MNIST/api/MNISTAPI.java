package com.redhat.xpaas.rad.MNIST.api;

import java.util.Map;

public interface MNISTAPI {

  void loadPage();
  boolean drawThree(Long timeForResults, int fetchResultsAttempts);
  Map<Integer, Double> modelResults(int modelType);
}
