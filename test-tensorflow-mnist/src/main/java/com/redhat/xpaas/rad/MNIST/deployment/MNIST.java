package com.redhat.xpaas.rad.MNIST.deployment;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.MNIST.api.MNISTWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MNIST {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private static final Long TIMEOUT = RadConfiguration.timeout();
  private static final String appTemplate = "/MNIST/web-application-template.yaml";
  private static final String tensorflowServingTemplate = "/MNIST/tensorflow-serving-template.yaml";


  public static MNISTWebUI deployMNIST() throws TimeoutException, InterruptedException {
    Template app = openshift.withAdminUser(client ->
      client.inNamespace(NAMESPACE).templates().load(MNIST.class.getResourceAsStream(appTemplate)).createOrReplace()
    );

    Template tensorflowServing = openshift.withAdminUser(client ->
      client.inNamespace(NAMESPACE).templates().load(MNIST.class.getResourceAsStream(tensorflowServingTemplate)).createOrReplace()
    );

    Map<String, String> endpointAParameters = new HashMap<>();
    endpointAParameters.put("APPLICATION_NAME", "tf-reg");
    endpointAParameters.put("SOURCE_REPOSITORY", "https://github.com/sub-mod/mnist-models");
    endpointAParameters.put("SOURCE_DIRECTORY", "regression");

    Map<String, String> endpointBParameters = new HashMap<>();
    endpointBParameters.put("APPLICATION_NAME", "tf-cnn");
    endpointBParameters.put("SOURCE_REPOSITORY", "https://github.com/sub-mod/mnist-models");
    endpointBParameters.put("SOURCE_DIRECTORY", "cnn");

    Map<String,String> appParameters = new HashMap<>();
    appParameters.put("APPLICATION_NAME", "mnist-app");
    appParameters.put("PREDICTION_SERVICE1", "tf-reg");
    appParameters.put("PREDICTION_SERVICE2", "tf-cnn");

    openshift.loadTemplate(app, appParameters);
    openshift.loadTemplate(tensorflowServing, endpointAParameters);
    openshift.loadTemplate(tensorflowServing, endpointBParameters);

    if(!WaitUtil.waitForActiveBuildsToComplete() ||
      !WaitUtil.waitForPodsToReachRunningState("appName", "tf-cnn", 1) ||
      !WaitUtil.waitForPodsToReachRunningState("appName", "tf-reg", 1) ||
      !WaitUtil.waitForPodsToReachRunningState("appid", "mnist-app-mnist-app", 1)){
      throw new IllegalStateException(LoggerUtil.openshiftError("mnist build/deployment", "pod/build"));
    }

    return MNISTWebUI.getInstance(openshift.appDefaultHostNameBuilder("mnist-app"));
  }
}
