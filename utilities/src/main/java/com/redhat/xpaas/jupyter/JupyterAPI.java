package com.redhat.xpaas.jupyter;

import com.redhat.xpaas.jupyter.entity.CodeCell;

public interface JupyterAPI {

  void login(String password);

  void loadProject(String projectName);

  void loadProjectByURL(String projectName);

  CodeCell getNthCodeCell(int n);

  void webDriverCleanup();
}
