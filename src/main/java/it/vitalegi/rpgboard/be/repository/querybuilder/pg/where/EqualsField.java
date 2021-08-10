package it.vitalegi.rpgboard.be.repository.querybuilder.pg.where;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;

import java.util.List;

public class EqualsField implements Renderer {

  protected String alias1;
  protected String field1;
  protected String alias2;
  protected String field2;

  public EqualsField(String alias1, String field1, String alias2, String field2) {
    this.alias1 = alias1;
    this.field1 = field1;
    this.alias2 = alias2;
    this.field2 = field2;
  }

  @Override
  public String render(List<TableInstance> instances) {
    StringBuilder sb = new StringBuilder();
    sb.append(QueryTool.aliasDotName(alias1, field1, instances));
    sb.append("=");
    sb.append(QueryTool.aliasDotName(alias2, field2, instances));
    return sb.toString();
  }
}
