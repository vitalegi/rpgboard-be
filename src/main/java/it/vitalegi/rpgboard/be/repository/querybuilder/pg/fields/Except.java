package it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields;

import java.util.List;

public class Except extends AbstractPicker {
  List<String> fields;

  public Except(String alias, List<String> fields) {
    super(alias);
    this.fields = fields;
  }

  @Override
  protected boolean filter(String field) {
    return !fields.contains(field);
  }
}
