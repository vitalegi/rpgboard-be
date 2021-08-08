package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

public class Pair<E, T> {
  E entry1;
  T entry2;

  public Pair(E entry1, T entry2) {
    this.entry1 = entry1;
    this.entry2 = entry2;
  }

  public E getEntry1() {
    return entry1;
  }

  public void setEntry1(E entry1) {
    this.entry1 = entry1;
  }

  public T getEntry2() {
    return entry2;
  }

  public void setEntry2(T entry2) {
    this.entry2 = entry2;
  }
}
