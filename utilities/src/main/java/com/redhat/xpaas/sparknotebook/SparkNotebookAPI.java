package com.redhat.xpaas.sparknotebook;

import com.redhat.xpaas.sparknotebook.entity.CodeCell;

public interface SparkNotebookAPI {

  void loadProjectByURL(String projectName);

  CodeCell getNthCodeCell(int n);

  void webDriverCleanup();
}
