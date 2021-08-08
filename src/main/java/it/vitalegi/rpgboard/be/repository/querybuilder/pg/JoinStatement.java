package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JoinStatement {

  protected SelectStatement left;
  protected SelectStatement right;
  protected List<Pair<TableField, TableField>> on;

  public JoinStatement(SelectStatement left, SelectStatement right) {
    this.left = left;
    this.right = right;
    on = new ArrayList<>();
  }

  public JoinStatement addEquals(String leftField, String rightField) {
    on.add(
        new Pair<>(
            new TableField(left.factory, leftField), new TableField(right.factory, rightField)));
    return this;
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("JOIN ");
    sb.append(buildTableName(right));
    sb.append(" ON ");
    sb.append(on.stream().map(this::map).collect(Collectors.joining(" AND ")));
    return sb.toString();
  }

  protected String buildTableName(SelectStatement table) {
    if (StringUtil.isNullOrEmpty(table.factory.alias)) {
      return table.factory.tableName;
    }
    return table.factory.tableName + " as " + table.factory.alias;
  }

  protected String map(Pair<TableField, TableField> onClause) {
    return onClause.entry1.build() + "=" + onClause.entry2.build();
  }
}
