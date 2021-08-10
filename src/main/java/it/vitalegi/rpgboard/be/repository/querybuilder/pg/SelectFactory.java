package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.from.FromClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.groupby.GroupByClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.selectvalues.SelectedValues;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SelectFactory {
  Logger log = LoggerFactory.getLogger(this.getClass());

  List<TableInstance> tableInstances;
  SelectedValues selectedValues;
  FromClause fromClause;
  WhereClause whereClause;
  GroupByClause groupBy;

  public SelectFactory() {
    tableInstances = new ArrayList<>();
    selectedValues = new SelectedValues().all(null);
  }

  public static SelectFactory init(Table table, String alias) {
    return new SelectFactory().table(table, alias);
  }

  public static SelectFactory init(Table table) {
    return new SelectFactory().table(table, "t1");
  }

  public SelectFactory table(Table table, String alias) {
    tableInstances.add(TableInstance.init(table, alias));
    return this;
  }

  public SelectFactory values(SelectedValues selectedValues) {
    this.selectedValues = selectedValues;
    return this;
  }

  public SelectFactory where(WhereClause whereClause) {
    this.whereClause = whereClause;
    return this;
  }

  public SelectFactory groupBy(GroupByClause groupBy) {
    this.groupBy = groupBy;
    return this;
  }

  public SelectFactory from(FromClause fromClause) {
    this.fromClause = fromClause;
    return this;
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    sb.append(selectedValues.render(tableInstances));
    if (fromClause == null) {
      fromClause = new FromClause(tableInstances.get(0).getAlias());
    }
    sb.append(fromClause.render(tableInstances));
    if (whereClause != null) {
      sb.append(whereClause.render(tableInstances));
    }
    if (groupBy != null) {
      sb.append(groupBy.render(tableInstances));
    }
    sb.append(";");

    return sb.toString();
  }
}
