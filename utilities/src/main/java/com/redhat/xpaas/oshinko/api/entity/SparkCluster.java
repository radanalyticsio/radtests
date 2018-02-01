package com.redhat.xpaas.oshinko.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Java object representing Oshinko JSON object
 */
@Getter
@Setter
public class SparkCluster {
  @JsonProperty("name")
  private String clusterName;
  @JsonProperty("mastersCount")
  private int mastersCount;
  @JsonProperty("workersCount")
  private int workersCount;

  @JsonProperty("namespace")
  private String namespace;
  @JsonProperty("href")
  private String href;
  @JsonProperty("image")
  private String image;
  @JsonProperty("masterUrl")
  private String masterUrl;
  @JsonProperty("masterWebUrl")
  private String masterWebUrl;
  @JsonProperty("status")
  private String status;

  @JsonProperty("Config")
  private SparkConfig sparkConfig;
  @JsonProperty("Pods")
  private List<SparkPod> sparkPods;

  public static List<SparkCluster> sparkClustersFromJson(String json) {

    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    SparkCluster[] clusters = null;

    try {
      clusters = mapper.readValue(json, SparkCluster[].class);
    } catch (IOException e) {
      throw new IllegalStateException("Can't parse JSON file to array of SparkCluster", e);
    }

    if(clusters == null){
      return  null;
    }

    return Arrays.asList(clusters);
  }
}


