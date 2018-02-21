package com.redhat.xpaas.sparknotebook.entity;

public interface CodeCell {

  CodeCell runCell();

  String getOutput();

  boolean outputHasErrors();

}
