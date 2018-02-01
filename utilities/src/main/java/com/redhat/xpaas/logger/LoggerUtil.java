package com.redhat.xpaas.logger;


import com.redhat.xpaas.RadConfiguration;

public class LoggerUtil {
  private final String projectName;


  public LoggerUtil(String projectName){
    this.projectName = projectName;
  }

  public String start(String message){
    StringBuilder sb = preMessageBuild(message).append("status=START");
    return String.valueOf(sb);
  }

  public String start(String message, Long time){
    StringBuilder sb = preMessageBuild(message, time).append("status=START");
    return String.valueOf(sb);
  }

  public String finish(String message){
    StringBuilder sb = preMessageBuild(message).append("status=FINISH");
    return String.valueOf(sb);
  }

  public String finish(String message, Long time){
    StringBuilder sb = preMessageBuild(message, time).append("status=FINISH");
    return String.valueOf(sb);
  }

  public String failed(String message){
    StringBuilder sb = preMessageBuild(message).append("status=FAILED");
    return String.valueOf(sb);
  }

  public String failed(String message, Long time){
    StringBuilder sb = preMessageBuild(message, time).append("status=FAILED");
    return String.valueOf(sb);
  }

  public String passed(String message){
    StringBuilder sb = preMessageBuild(message).append("status=PASSED");
    return String.valueOf(sb);
  }

  public String passed(String message, Long time){
    StringBuilder sb = preMessageBuild(message, time).append("status=PASSED");
    return String.valueOf(sb);
  }

  public String error(String message){
    StringBuilder sb = preMessageBuild(message).append("status=ERROR");
    return String.valueOf(sb);
  }

  public String error(String message, Long time){
    StringBuilder sb = preMessageBuild(message, time).append("status=ERROR");
    return String.valueOf(sb);
  }

  private StringBuilder preMessageBuild(String message){
    StringBuilder msg = new StringBuilder();
    msg.append("openshiftVersion=").append(RadConfiguration.openshiftVersion()).append(" ");
    msg.append("project=").append(projectName).append(" ");
    msg.append("action=").append(message).append(" ");
    return msg;
  }

  private StringBuilder preMessageBuild(String message, Long Time){
    StringBuilder msg = new StringBuilder();
    msg.append(preMessageBuild(message));
    msg.append("time=").append(Time).append(" ");
    return msg;
  }
}
