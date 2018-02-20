package com.redhat.xpaas.oshinko.api;

public interface OshinkoAPI {

	boolean createCluster(String clusterName);

	boolean createCluster(String clusterName, int workersCount);

	boolean createCluster(String clusterName, int workersCount, int mastersCount, String masterConfig, String workerConfig, String storedConfig, String sparkImage);

	boolean scaleCluster(String clusterName, int workersCount);

	boolean deleteCluster(String clusterName);
}
