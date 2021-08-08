package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectStatement extends AbstractPreparedStatement {
  Logger log = LoggerFactory.getLogger(SelectStatement.class);

  FieldList<SelectStatement> selectedValues;
  WhereClause<SelectStatement> whereClause;
  List<JoinStatement> joinStatements;
  GroupByStatement groupBy;

  public SelectStatement(PreparedStatementFactory factory) {
    super(factory);
    selectedValues = FieldList.allFields(factory, this);
    whereClause = new WhereClause<SelectStatement>(factory, this);
    joinStatements = new ArrayList<>();
    groupBy = new GroupByStatement();
  }

  public FieldList<SelectStatement> values() {
    return selectedValues;
  }

  public WhereClause<SelectStatement> where() {
    return whereClause;
  }

  public SelectStatement groupBy(GroupByStatement groupBy) {
    this.groupBy = groupBy;
    return this;
  }

  public SelectStatement join(JoinStatement joinStatement) {
    joinStatements.add(joinStatement);
    return this;
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append(buildSelectValues());
    sb.append(" FROM ");
    sb.append(buildTableNames());
    sb.append(buildWhereClause());
    sb.append(buildGroupBy());
    sb.append(";");

    return sb.toString();
  }

  protected String buildSelectValues() {
    List<String> values = new ArrayList<>();
    values.add(selectedValues.buildSelect());
    if (!joinStatements.isEmpty()) {
      values.addAll(
          this.joinStatements.stream()
              .map(s -> s.right.selectedValues.buildSelect())
              .collect(Collectors.toList()));
    }
    return values.stream().collect(Collectors.joining(", "));
  }

  protected String buildWhereClause() {
    List<String> values = new ArrayList<>();
    values.add(whereClause.build(false));
    if (!joinStatements.isEmpty()) {
      values.addAll(
          this.joinStatements.stream()
              .map(s -> s.right.whereClause.build(false))
              .collect(Collectors.toList()));
    }
    String whereClause =
        values.stream()
            .map(String::trim)
            .filter(StringUtil::isNotNullOrEmpty)
            .collect(Collectors.joining(" AND "));
    if (whereClause.length() == 0) {
      return "";
    }
    return " WHERE " + whereClause;
  }

  protected String buildTableNames() {
    StringBuilder sb = new StringBuilder();
    sb.append(buildTableName());
    if (joinStatements.isEmpty()) {
      return sb.toString();
    }
    sb.append(" ");
    sb.append(joinStatements.stream().map(JoinStatement::build).collect(Collectors.joining(" ")));
    return sb.toString().trim();
  }

  protected String buildTableName() {
    if (StringUtil.isNullOrEmpty(factory.alias)) {
      return factory.tableName;
    }
    return factory.tableName + " as " + factory.alias;
  }

  protected String buildGroupBy() {
    String result = groupBy.build();
    if (StringUtil.isNotNullOrEmpty(result)) {
      return " "+result;
    }
    return "";
  }
}
