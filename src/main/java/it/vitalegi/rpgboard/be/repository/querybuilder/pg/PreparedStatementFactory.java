package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PreparedStatementFactory {
  protected List<String> primaryKeys;
  protected List<String> autoGenerated;
  protected List<String> fields;
  protected String tableName;
  protected String alias;

  private PreparedStatementFactory() {
    primaryKeys = new ArrayList<>();
    autoGenerated = new ArrayList<>();
    fields = new ArrayList<>();
  }

  public static PreparedStatementFactory init() {
    return new PreparedStatementFactory();
  }

  public PreparedStatementFactory alias(String alias) {
    PreparedStatementFactory factory = new PreparedStatementFactory();
    factory.primaryKeys = new ArrayList<>(primaryKeys);
    factory.autoGenerated = new ArrayList<>(autoGenerated);
    factory.fields = new ArrayList<>(fields);
    factory.tableName = tableName;
    factory.alias = alias;
    return factory;
  }

  public PreparedStatementFactory primaryKeys(List<String> keys) {
    primaryKeys = new ArrayList<>(keys);
    return this;
  }

  public PreparedStatementFactory autoGenerated(String key) {
    autoGenerated = Collections.singletonList(key);
    return this;
  }

  public PreparedStatementFactory primaryKey(String key) {
    primaryKeys = Collections.singletonList(key);
    return this;
  }

  public List<String> primaryKeys() {
    return primaryKeys;
  }

  public List<String> autoGeneratedKeys() {
    return autoGenerated;
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
    return new InsertStatement(alias(null));
  }

  public UpdateStatement update() {
    return new UpdateStatement(alias(null));
  }

  public DeleteStatement delete() {
    return new DeleteStatement(alias(null));
  }

  public SelectStatement select() {
    return new SelectStatement(alias("t1"));
  }

  public SelectStatement select(String alias) {
    return new SelectStatement(alias(alias));
  }
}
