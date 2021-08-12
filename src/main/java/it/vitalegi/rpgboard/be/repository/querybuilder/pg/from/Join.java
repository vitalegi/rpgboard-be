package it.vitalegi.rpgboard.be.repository.querybuilder.pg.from;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsField;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsPlaceholder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Join implements Renderer {
  protected JoinType joinType;
  protected String aliasRight;
  protected List<Renderer> on;

  public Join(String aliasRight) {
    this(aliasRight, new ArrayList<>());
  }

  public Join(String aliasRight, List<EqualsField> on) {
    this(JoinType.INNER_JOIN, aliasRight, on);
  }

  public Join(JoinType joinType, String aliasRight) {
    this(joinType, aliasRight, new ArrayList<>());
  }

  public Join(JoinType joinType, String aliasRight, List<EqualsField> on) {
    this.aliasRight = aliasRight;
    this.on = new ArrayList<>();
    this.on.addAll(on);
    this.joinType = joinType;
  }

  public Join and(String alias1, String field1, String alias2, String field2) {
    on.add(new EqualsField(alias1, field1, alias2, field2));
    return this;
  }

  public Join andPlaceholder(String alias, String field) {
    on.add(
        new EqualsPlaceholder(FieldsPicker.exact(alias, Collections.singletonList(field)), field));
    return this;
  }

  @Override
  public String render(List<TableInstance> instances) {
    TableInstance rightInstance = QueryTool.getByAlias(aliasRight, instances);

    StringBuilder sb = new StringBuilder();
    sb.append(joinType.render())
        .append(" ")
        .append(
            QueryTool.nameAsAlias(
                rightInstance.getAlias(), rightInstance.getTableName(), instances));
    sb.append(QueryTool.renderer(instances, on, " AND ", " ON ", ""));
    return sb.toString();
  }
}
