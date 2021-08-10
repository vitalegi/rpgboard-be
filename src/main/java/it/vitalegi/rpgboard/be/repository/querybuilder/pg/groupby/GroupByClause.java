package it.vitalegi.rpgboard.be.repository.querybuilder.pg.groupby;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;

import java.util.List;

public class GroupByClause implements Renderer {
  protected List<GroupByTableColumnValue> groupBy;

  public GroupByClause(List<GroupByTableColumnValue> groupBy) {
    this.groupBy = groupBy;
  }

  @Override
  public String render(List<TableInstance> instances) {
    return QueryTool.renderer(instances, groupBy, ", ", " GROUP BY ", "");
  }
}
