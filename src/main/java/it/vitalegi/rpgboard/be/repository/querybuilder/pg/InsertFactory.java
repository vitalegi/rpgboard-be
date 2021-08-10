package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.insert.InsertValuesClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.returning.ReturningClause;

import java.util.Collections;
import java.util.List;

public class InsertFactory {
  TableInstance tableInstance;
  InsertValuesClause insertValuesClause;
  ReturningClause returningClause;

  public InsertFactory(Table table) {
    tableInstance = TableInstance.init(table, null);
    returning(new ReturningClause(FieldsPicker.all()));
    values(new InsertValuesClause().all());
  }

  public static InsertFactory init(Table table) {
    return new InsertFactory(table);
  }

  public InsertFactory values(InsertValuesClause insertValuesClause) {
    this.insertValuesClause = insertValuesClause;
    return this;
  }

  public InsertFactory returning(ReturningClause returningClause) {
    this.returningClause = returningClause;
    return this;
  }

  public String build() {
    List<TableInstance> instances = Collections.singletonList(tableInstance);
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO ");
    sb.append(tableInstance.getTableName());
    sb.append(insertValuesClause.render(instances));
    if (returningClause != null) {
      sb.append(returningClause.render(instances));
    }
    sb.append(";");
    return sb.toString();
  }
}
