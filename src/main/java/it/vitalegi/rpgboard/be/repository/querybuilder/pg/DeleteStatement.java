package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import java.util.ArrayList;
import java.util.List;

public class DeleteStatement extends AbstractPreparedStatement {
  List<String> values = new ArrayList<>();
  FieldList<DeleteStatement> returning;
  WhereClause<DeleteStatement> whereClause;

  public DeleteStatement(PreparedStatementFactory factory) {
    super(factory);
    returning = FieldList.allFields(factory, this);
    whereClause = new WhereClause<DeleteStatement>(factory, this);
  }

  public FieldList<DeleteStatement> returning() {
    return returning;
  }

  public WhereClause<DeleteStatement> where() {
    return whereClause;
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("DELETE FROM ");
    sb.append(factory.tableName);
    sb.append(whereClause.build());
    sb.append(returning.buildReturning());
    sb.append(";");

    return sb.toString();
  }
}
