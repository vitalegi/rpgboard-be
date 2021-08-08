package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WhereClause<E extends AbstractPreparedStatement> extends AbstractPreparedStatement {
  List<String> clauses;
  E parent;

  public WhereClause(PreparedStatementFactory factory, E builder) {
    super(factory);
    parent = builder;
    clauses = new ArrayList<>();
  }

  public WhereClause<E> areEqualToPlaceholder(List<String> fields) {
    this.clauses.add(
        fields.stream().map(this::equalToPlaceholder).collect(Collectors.joining(" AND ")));
    return this;
  }

  public WhereClause<E> isEqualsToPlaceholder(String field) {
    this.clauses.add(super.equalToPlaceholder(field));
    return this;
  }

  public WhereClause<E> allEqualToPlaceholder() {
    return areEqualToPlaceholder(factory.fields);
  }

  public E end() {
    return parent;
  }

  protected String build(boolean includeWhereKeyword) {
    if (clauses.isEmpty()) {
      return "";
    }
    String statement = String.join(" AND ", clauses);
    if (includeWhereKeyword) {
      return " WHERE " + statement;
    }
    return statement;
  }

  protected String build() {
    return build(true);
  }
}
