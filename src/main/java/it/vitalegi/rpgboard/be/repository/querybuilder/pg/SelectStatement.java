package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;

public class SelectStatement extends AbstractPreparedStatement {
  FieldList<SelectStatement> selectedValues;
  WhereClause<SelectStatement> whereClause;

  public SelectStatement(PreparedStatementFactory factory) {
    super(factory);
    selectedValues = FieldList.allFields(factory, this);
    whereClause = new WhereClause<SelectStatement>(factory, this);
  }

  public FieldList<SelectStatement> values() {
    return selectedValues;
  }

  public WhereClause<SelectStatement> where() {
    return whereClause;
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append(selectedValues.buildSelect());
    sb.append(" FROM ");
    sb.append(buildTableName());
    sb.append(whereClause.build());
    sb.append(";");

    return sb.toString();
  }

  protected String buildTableName() {
    if (StringUtil.isNullOrEmpty(factory.alias)) {
      return factory.tableName;
    }
    return factory.tableName + " as " + factory.alias;
  }
}
