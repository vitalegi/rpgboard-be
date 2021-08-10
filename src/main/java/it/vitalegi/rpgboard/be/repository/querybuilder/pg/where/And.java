package it.vitalegi.rpgboard.be.repository.querybuilder.pg.where;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;

import java.util.ArrayList;
import java.util.List;

public class And implements Renderer {

  protected List<Renderer> renderers;

  public And(List<Renderer> renderers) {
    this.renderers = renderers;
  }

  public static And init(Renderer renderer) {
    return new And(new ArrayList<>());
  }

  public And and(Renderer renderer) {
    this.renderers.add(renderer);
    return this;
  }

  @Override
  public String render(List<TableInstance> instances) {
    return QueryTool.renderer(instances, renderers, " AND ", "(", ")");
  }
}
