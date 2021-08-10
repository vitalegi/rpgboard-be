package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;

public class TableField {
  TableInstance tableInstance;
  String field;

  public TableField(TableInstance tableInstance, String field) {
    this.tableInstance = tableInstance;
    this.field = field;
  }

  public String build() {
    if (StringUtil.isNullOrEmpty(tableInstance.getAlias())) {
      return field;
    }
    return tableInstance.getAlias() + "." + field;
  }
}
