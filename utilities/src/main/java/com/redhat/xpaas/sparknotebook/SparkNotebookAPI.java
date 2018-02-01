package com.redhat.xpaas.sparknotebook;

import com.redhat.xpaas.sparknotebook.entity.CodeCell;

public interface SparkNotebookAPI {

  void login(String password);

  void loadProjectByURL(String projectName);

  CodeCell getNthCodeCell(int n);


  void webDriverCleanup();
}
