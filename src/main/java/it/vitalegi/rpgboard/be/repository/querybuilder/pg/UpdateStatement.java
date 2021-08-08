package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

public class UpdateStatement extends AbstractPreparedStatement {
  FieldList<UpdateStatement> setValues;
  FieldList<UpdateStatement> returning;
  WhereClause<UpdateStatement> whereClause;

  public UpdateStatement(PreparedStatementFactory factory) {
    super(factory);
    setValues = FieldList.allFields(factory, this);
    returning = FieldList.allFields(factory, this);
    whereClause = new WhereClause<UpdateStatement>(factory, this);
  }

  public FieldList<UpdateStatement> setValues() {
    return setValues;
  }

  public FieldList<UpdateStatement> returning() {
    return returning;
  }

  public WhereClause<UpdateStatement> where() {
    return whereClause;
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE ");
    sb.append(factory.tableName);
    sb.append(" SET ");
    sb.append(setValues.buildSet());
    sb.append(whereClause.build());
    sb.append(returning.buildReturning());
    sb.append(";");

    return sb.toString();
  }
}
