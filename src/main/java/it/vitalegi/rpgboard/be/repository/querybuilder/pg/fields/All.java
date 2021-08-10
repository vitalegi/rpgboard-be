package it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields;

public class All extends AbstractPicker {
  public All(String alias) {
    super(alias);
  }

  @Override
  protected boolean filter(String field) {
    return true;
  }
}
