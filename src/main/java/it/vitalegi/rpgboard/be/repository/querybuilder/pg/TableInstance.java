package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

public class TableInstance extends Table {
  protected String alias;

  public static TableInstance init(Table table, String alias) {
    TableInstance instance = new TableInstance();
    table.hardCopy(instance);
    instance.setAlias(alias);
    return instance;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }
}
