package com.redhat.xpaas.rad.ophicleide.api;

import com.redhat.xpaas.rad.ophicleide.api.entity.QueryResults;

public interface OphicleideAPI {
  String trainModel(String name, String urls);

  boolean deleteModel(String modelName);

  QueryResults createQuery(String modelName, String modleQuery);
}
