package it.vitalegi.rpgboard.be.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class PreparedStatementBuilder {
  protected List<String> fields = new ArrayList<>();
  protected String tableName;

  public static PreparedStatementBuilder init() {
    return new PreparedStatementBuilder();
  }

  public PreparedStatementBuilder fields(String... names) {
    fields.addAll(Arrays.asList(names));
    return this;
  }

  public PreparedStatementBuilder tableName(String tableName) {
    this.tableName = tableName;
    return this;
  }

  public String add(List<String> skipFields) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO ");
    sb.append(tableName);
    List<String> insertFields = diff(fields, skipFields);
    sb.append(insertFields.stream().collect(join(", ", "(", ")")));
    sb.append(" VALUES ");
    sb.append(insertFields.stream().map(this::placeholder).collect(join(", ", "(", ")")));
    sb.append(returning(fields));
    sb.append(";");
    return sb.toString();
  }

  public String updateAllById(String name) {
    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE ");
    sb.append(tableName);
    sb.append(" SET ");
    sb.append(fields.stream().map(this::eq).collect(Collectors.joining(", ")));

    sb.append(" WHERE ");
    sb.append(eq(name));
    sb.append(returning(fields));
    sb.append(";");
    return sb.toString();
  }

  public String deleteAllById(String name) {
    StringBuilder sb = new StringBuilder();
    sb.append("DELETE FROM ");
    sb.append(tableName);

    sb.append(" WHERE ");
    sb.append(eq(name));

    sb.append(returning(fields));
    sb.append(";");
    return sb.toString();
  }

  public String searchEquals(List<String> fields) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append(String.join(",", this.fields));
    sb.append(" FROM ");
    sb.append(tableName);

    if (!fields.isEmpty()) {
      sb.append(" WHERE ");
      sb.append(fields.stream().map(this::eq).collect(Collectors.joining(" AND ")));
    }
    sb.append(";");
    return sb.toString();
  }

  protected String returning(List<String> fields) {
    return " RETURNING " + String.join(", ", fields);
  }

  protected String eq(String field) {
    return field + "=#{" + field + "}";
  }

  protected String placeholder(String field) {
    return "#{" + field + "}";
  }

  protected Collector<CharSequence, ?, String> join(
      String delimiter, String prefix, String suffix) {
    return Collectors.joining(delimiter, prefix, suffix);
  }

  protected List<String> diff(List<String> list, List<String> toRemove) {
    return list.stream().filter(el -> !toRemove.contains(el)).collect(Collectors.toList());
  }
}
