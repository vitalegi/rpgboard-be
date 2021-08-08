package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;

public class TableField extends AbstractPreparedStatement {
  String field;

  public TableField(PreparedStatementFactory factory, String field) {
    super(factory);
    this.field = field;
  }

  public String build() {
    if (StringUtil.isNullOrEmpty(factory.alias)) {
      return field;
    }
    return factory.alias + "." + field;
  }
}
