package com.redhat.xpaas.rad.ophicleide.api.entity;

public class Result<L, R> {
  private final L left;
  private final R right;

  public Result(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public L getLeft() { return left; }
  public R getRight() { return right; }

  @Override
  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Result)) return false;
    Result pairo = (Result) o;
    return this.left.equals(pairo.getLeft()) &&
      this.right.equals(pairo.getRight());
  }

  @Override
  public String toString(){
    return String.valueOf(left) + ": " + right;
  }
}


