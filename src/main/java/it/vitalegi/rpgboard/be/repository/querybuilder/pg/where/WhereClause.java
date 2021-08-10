package it.vitalegi.rpgboard.be.repository.querybuilder.pg.where;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.Arrays;
import java.util.List;

public class WhereClause implements Renderer {
  protected Renderer clauses;

  public WhereClause(Renderer clauses) {
    this.clauses = clauses;
  }

  public static WhereClause init(And and) {
    return new WhereClause(and);
  }

  public static WhereClause and(Renderer... clauses) {
    return new WhereClause(new And(Arrays.asList(clauses)));
  }

  @Override
  public String render(List<TableInstance> instances) {
    String where = clauses.render(instances);
    if (StringUtil.isNotNullOrEmpty(where)) {
      return " WHERE " + where;
    }
    return "";
  }
}
