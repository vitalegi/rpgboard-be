package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.from.FromClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.returning.ReturningClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;

import java.util.ArrayList;
import java.util.List;

public class DeleteFactory {
  List<TableInstance> tableInstances;
  FromClause fromClause;
  WhereClause whereClause;
  ReturningClause returningClause;

  public DeleteFactory() {
    tableInstances = new ArrayList<>();
    from(new FromClause());
    returning(new ReturningClause(FieldsPicker.all()));
  }

  public static DeleteFactory init(Table table) {
    return new DeleteFactory().table(table, null);
  }

  public static DeleteFactory init(Table table, String alias) {
    return new DeleteFactory().table(table, alias);
  }

  public DeleteFactory table(Table table, String alias) {
    tableInstances.add(TableInstance.init(table, alias));
    return this;
  }

  public DeleteFactory where(WhereClause whereClause) {
    this.whereClause = whereClause;
    return this;
  }

  public DeleteFactory from(FromClause fromClause) {
    this.fromClause = fromClause;
    return this;
  }

  public DeleteFactory returning(ReturningClause returningClause) {
    this.returningClause = returningClause;
    return this;
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("DELETE");
    sb.append(fromClause.render(tableInstances));
    if (whereClause != null) {
      sb.append(whereClause.render(tableInstances));
    }
    if (returningClause != null) {
      sb.append(returningClause.render(tableInstances));
    }
    sb.append(";");
    return sb.toString();
  }
}
