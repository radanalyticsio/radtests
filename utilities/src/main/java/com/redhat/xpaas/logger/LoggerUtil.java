package com.redhat.xpaas.logger;

import com.redhat.xpaas.RadConfiguration;

public class LoggerUtil {
  private final String projectName;

  public LoggerUtil(String projectName){
    this.projectName = projectName;
  }

  public String start(String action){
    StringBuilder sb = preMessageBuild(action).append("status=START");
    return String.valueOf(sb);
  }

  public String start(String action, Long time){
    StringBuilder sb = preMessageBuild(action, time).append("status=START");
    return String.valueOf(sb);
  }

  public String finish(String action, Long time){
    StringBuilder sb = preMessageBuild(action, time).append("status=FINISH");
    return String.valueOf(sb);
  }

  public String failed(String action, String failMsg, Long time){
    StringBuilder sb = preMessageBuild(action, time)
      .append("status=FAILED").append(" ")
      .append("failmsg=")
      .append(failMsg);
    return String.valueOf(sb);
  }

  public String passed(String action, Long time){
    StringBuilder sb = preMessageBuild(action, time).append("status=PASSED");
    return String.valueOf(sb);
  }

  public String error(String action, String errorMsg){
    StringBuilder sb = preMessageBuild(action)
      .append("status=ERROR").append(" ")
      .append("errorMsg=")
      .append(errorMsg);
    return String.valueOf(sb);
  }

  public static String openshiftError(String action, String object){
    return String.format("Error while performing [%s]. Check [%s] logs at %s in project %s for more details.",
      action, object, RadConfiguration.masterUrl(), RadConfiguration.masterNamespace());
  }

  private StringBuilder preMessageBuild(String action){
    StringBuilder msg = new StringBuilder();
    msg.append("openshiftVersion=").append(RadConfiguration.openshiftVersion()).append(" ");
    msg.append("project=").append(projectName).append(" ");
    msg.append("action=").append(action).append(" ");
    return msg;
  }

  private StringBuilder preMessageBuild(String message, Long Time){
    StringBuilder msg = new StringBuilder();
    msg.append(preMessageBuild(message));
    msg.append("time=").append(Time).append(" ");
    return msg;
  }
}
