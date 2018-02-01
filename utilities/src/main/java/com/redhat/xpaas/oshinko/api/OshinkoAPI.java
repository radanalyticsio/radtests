package com.redhat.xpaas.oshinko.api;

import com.redhat.xpaas.oshinko.api.entity.SparkCluster;

import java.util.List;

public interface OshinkoAPI {

	boolean createCluster(String clusterName);

	boolean createCluster(String clusterName, int workersCount);

	boolean createCluster(String clusterName, int workersCount, int mastersCount, String masterConfig, String workerConfig, String storedConfig, String sparkImage);

	SparkCluster getCluster(String clusterName);

	List<SparkCluster> listClusters();

	boolean scaleCluster(String clusterName, int workersCount);

	boolean deleteCluster(String clusterName);
}
