package it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields;

public class Field {
  String alias;
  String field;

  public Field(String alias, String field) {
    this.alias = alias;
    this.field = field;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }
}
