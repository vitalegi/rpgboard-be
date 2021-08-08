package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GroupByStatement {
  protected List<GroupByTableField> groupBy;

  public GroupByStatement() {
    groupBy = new ArrayList<>();
  }

  public GroupByStatement add(GroupByTableField field) {
    groupBy.add(field);
    return this;
  }

  protected String build() {
    String clauses =
        groupBy.stream().map(GroupByTableField::build).collect(Collectors.joining(", "));
    if (StringUtil.isNotNullOrEmpty(clauses)) {
      return "GROUP BY " + clauses;
    }
    return "";
  }
}
