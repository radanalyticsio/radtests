package com.redhat.xpaas.rad.ophicleide.api.entity;

import java.util.ArrayList;

public class QueryResults {
  private ArrayList<Result> results;
  private final String modelName;

  public QueryResults(String modelName){
    this.modelName = modelName;
    this.results = new ArrayList<>();
  }

  public void addResult(Result result){
    results.add(result);
  }

  public ArrayList<Result> getResult(){
    return results;
  }

  public String getModelName(){
    return modelName;
  }

  @Override
  public String toString(){
    StringBuilder result = new StringBuilder();
    for (Result r : results){
      result.append(r.toString());
      result.append("|");
    }
    return result.toString();
  }
}
