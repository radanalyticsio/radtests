package com.redhat.xpaas.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogWrapper {
  private final Logger log;
  private final LoggerUtil logUtil;

  public LogWrapper(Class logClass, String projectName){
    log = LoggerFactory.getLogger(logClass);
    logUtil = new LoggerUtil(projectName);
  }

  public Logger getLogger(){
    return this.log;
  }

  public void start(String message){
    log.info(logUtil.start(message));
  }

  public void finish(String message, Long time){
    log.info(logUtil.finish(message, time));
  }

  public void passed(String message, Long time){
    log.info(logUtil.passed(message, time));
  }

  public void failed(String action, String failMsg, Long time){
    log.info(logUtil.failed(action, failMsg, time));
  }

  public void error(String action, String errorMsg){
    log.error(logUtil.error(action, errorMsg));
  }

  public void info(String message){
    log.info(logUtil.start(message));
  }

}
