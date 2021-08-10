package it.vitalegi.rpgboard.be.repository.querybuilder.pg.from;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;

import java.util.ArrayList;
import java.util.List;

public class FromClause implements Renderer {
  protected String firstAlias;
  protected List<Join> joins;

  public FromClause(String firstAlias, List<Join> joins) {
    this.firstAlias = firstAlias;
    this.joins = joins;
  }

  public FromClause(String alias) {
    this(alias, new ArrayList<>());
  }

  public FromClause() {
    this(null);
  }

  public FromClause join(Join join) {
    joins.add(join);
    return this;
  }

  @Override
  public String render(List<TableInstance> instances) {
    TableInstance instance = QueryTool.getByAlias(firstAlias, instances);
    return " FROM "
        + QueryTool.nameAsAlias(instance.getAlias(), instance.getTableName(), instances)
        + QueryTool.renderer(instances, joins, " ", " ", "");
  }
}
