package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;

public class TableField {
  String alias;
  String field;

  public TableField(String alias, String field) {
    this.alias = alias;
    this.field = field;
  }

  public String build() {
    if (StringUtil.isNullOrEmpty(alias)) {
      return field;
    }
    return alias + "." + field;
  }
}
