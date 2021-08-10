package it.vitalegi.rpgboard.be.repository.querybuilder.pg.groupby;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.List;

public class GroupByTableColumnValue implements Renderer {
  String alias;
  String field;

  public GroupByTableColumnValue(String alias, String field) {
    this.alias = alias;
    this.field = field;
  }

  @Override
  public String render(List<TableInstance> instances) {
    StringBuilder sb = new StringBuilder();
    if (StringUtil.isNotNullOrEmpty(alias)) {
      sb.append(alias).append(".");
    }
    sb.append(field);
    return sb.toString();
  }
}
