package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.from.FromClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.returning.ReturningClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.updateset.SetClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;

import java.util.ArrayList;
import java.util.List;

public class UpdateFactory {
  List<TableInstance> tableInstances;
  FromClause fromClause;
  WhereClause whereClause;
  ReturningClause returningClause;
  SetClause setClause;

  public UpdateFactory() {
    tableInstances = new ArrayList<>();
    from(new FromClause());
    returning(new ReturningClause(FieldsPicker.all()));
  }

  public static UpdateFactory init(Table table) {
    return new UpdateFactory().table(table, null);
  }

  public UpdateFactory table(Table table, String alias) {
    tableInstances.add(TableInstance.init(table, alias));
    return this;
  }

  public UpdateFactory where(WhereClause whereClause) {
    this.whereClause = whereClause;
    return this;
  }

  public UpdateFactory from(FromClause fromClause) {
    this.fromClause = fromClause;
    return this;
  }

  public UpdateFactory set(SetClause setClause) {
    this.setClause = setClause;
    return this;
  }

  public UpdateFactory returning(ReturningClause returningClause) {
    this.returningClause = returningClause;
    return this;
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE ");
    sb.append(tableInstances.get(0).getTableName());
    sb.append(setClause.render(tableInstances));
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
