package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import java.util.ArrayList;
import java.util.List;

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
    sb.append(factory.tableName);
    sb.append(whereClause.build());
    sb.append(";");

    return sb.toString();
  }
}
