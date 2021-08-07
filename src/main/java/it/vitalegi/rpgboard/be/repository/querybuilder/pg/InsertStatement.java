package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

public class InsertStatement extends AbstractPreparedStatement {
  FieldList<InsertStatement> values;
  FieldList<InsertStatement> returning;

  public InsertStatement(PreparedStatementFactory factory) {
    super(factory);
    values = FieldList.allFields(factory, this);
    returning = FieldList.allFields(factory, this);
  }

  public FieldList<InsertStatement> values() {
    return values;
  }

  public FieldList<InsertStatement> returning() {
    return returning;
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO ");
    sb.append(factory.tableName);
    sb.append(values.buildInsertKeys());
    sb.append(" VALUES ");
    sb.append(values.buildInsertValues());
    sb.append(returning.buildReturning());
    sb.append(";");
    return sb.toString();
  }
}
