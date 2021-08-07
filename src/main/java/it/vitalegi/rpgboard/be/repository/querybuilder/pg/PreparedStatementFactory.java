package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PreparedStatementFactory {
  protected List<String> primaryKeys;
  protected List<String> fields;
  protected String tableName;

  private PreparedStatementFactory() {
    primaryKeys = new ArrayList<>();
    fields = new ArrayList<>();
  }

  public static PreparedStatementFactory init() {
    return new PreparedStatementFactory();
  }

  public PreparedStatementFactory primaryKeys(List<String> keys) {
    primaryKeys = new ArrayList<>(keys);
    return this;
  }
  public PreparedStatementFactory primaryKey(String key) {
    primaryKeys = Collections.singletonList(key);
    return this;
  }

  public List<String> primaryKeys() {
    return primaryKeys;
  }

  public PreparedStatementFactory fields(String... names) {
    fields = Arrays.asList(names);
    return this;
  }

  public PreparedStatementFactory tableName(String tableName) {
    this.tableName = tableName;
    return this;
  }

  public InsertStatement insert() {
    return new InsertStatement(this);
  }

  public UpdateStatement update() {
    return new UpdateStatement(this);
  }

  public DeleteStatement delete() {
    return new DeleteStatement(this);
  }

  public SelectStatement select() {
    return new SelectStatement(this);
  }
}
