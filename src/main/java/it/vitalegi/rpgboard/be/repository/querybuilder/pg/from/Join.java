package it.vitalegi.rpgboard.be.repository.querybuilder.pg.from;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsField;

import java.util.ArrayList;
import java.util.List;

public class Join implements Renderer {
  protected String aliasRight;
  protected List<EqualsField> on;

  public Join(String aliasRight) {
    this(aliasRight, new ArrayList<>());
  }

  public Join(String aliasRight, List<EqualsField> on) {
    this.aliasRight = aliasRight;
    this.on = on;
  }

  public Join and(String alias1, String field1, String alias2, String field2) {
    on.add(new EqualsField(alias1, field1, alias2, field2));
    return this;
  }

  @Override
  public String render(List<TableInstance> instances) {
    TableInstance rightInstance = QueryTool.getByAlias(aliasRight, instances);

    StringBuilder sb = new StringBuilder();
    sb.append("JOIN ")
        .append(
            QueryTool.nameAsAlias(
                rightInstance.getAlias(), rightInstance.getTableName(), instances));
    sb.append(QueryTool.renderer(instances, on, " AND ", " ON ", ""));
    return sb.toString();
  }
}
