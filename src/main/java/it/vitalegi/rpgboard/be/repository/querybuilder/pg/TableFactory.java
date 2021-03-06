package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import java.util.List;

public class TableFactory {

  protected Table table;

  private TableFactory() {
    table = new Table();
  }

  public static TableFactory init() {
    return new TableFactory();
  }

  public TableFactory primaryKeys(List<String> keys) {
    table.getPrimaryKeys().addAll(keys);
    return this;
  }

  public TableFactory autoGenerated(String key) {
    table.getAutoGenerated().add(key);
    return this;
  }

  public TableFactory primaryKey(String key) {
    table.getPrimaryKeys().add(key);
    return this;
  }

  public TableFactory fields(String... names) {
    for (String name : names) {
      table.getFields().add(name);
    }
    return this;
  }

  public TableFactory tableName(String tableName) {
    table.setTableName(tableName);
    return this;
  }

  public Table build() {
    return table;
  }
}
