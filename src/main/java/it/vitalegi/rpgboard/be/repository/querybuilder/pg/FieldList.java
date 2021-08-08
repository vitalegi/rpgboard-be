package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FieldList<E extends AbstractPreparedStatement> extends AbstractPreparedStatement {
  List<String> selectedFields = new ArrayList<>();
  E parent;

  private FieldList(PreparedStatementFactory factory, E builder, List<String> fields) {
    super(factory);
    parent = builder;
    selectedFields = fields;
  }

  public static <E extends AbstractPreparedStatement> FieldList<E> emptyFields(
      PreparedStatementFactory factory, E builder) {
    return new FieldList<>(factory, builder, new ArrayList<>());
  }

  public static <E extends AbstractPreparedStatement> FieldList<E> allFields(
      PreparedStatementFactory factory, E builder) {
    return new FieldList<>(factory, builder, new ArrayList<>(factory.fields));
  }

  public E exact(List<String> fields) {
    selectedFields = fields;
    return parent;
  }

  public E all() {
    selectedFields = factory.fields;
    return parent;
  }

  public E allExcept(List<String> skipFields) {
    selectedFields = diff(factory.fields, skipFields);
    return parent;
  }

  public E allExcept(String skipField) {
    return allExcept(Collections.singletonList(skipField));
  }

  protected String buildReturning() {
    return " RETURNING " + String.join(", ", selectedFields);
  }

  protected String buildSelect() {
    return String.join(", ", applyAlias(selectedFields, factory.alias));
  }

  protected String buildSet() {
    return selectedFields.stream().map(this::equalToPlaceholder).collect(Collectors.joining(", "));
  }

  protected String buildInsertKeys() {
    return selectedFields.stream().collect(join(", ", "(", ")"));
  }

  protected String buildInsertValues() {
    return selectedFields.stream().map(this::placeholder).collect(join(", ", "(", ")"));
  }

  protected List<String> applyAlias(List<String> fields, String alias) {
    return fields.stream().map(field -> applyAlias(field, alias)).collect(Collectors.toList());
  }

  protected String applyAlias(String field, String alias) {
    if (StringUtil.isNullOrEmpty(alias)) {
      return field;
    }
    return alias + "." + field;
  }
}
