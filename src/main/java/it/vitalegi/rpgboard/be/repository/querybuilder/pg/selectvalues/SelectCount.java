package it.vitalegi.rpgboard.be.repository.querybuilder.pg.selectvalues;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;

import java.util.List;

public class SelectCount implements Renderer {
  String as;

  public SelectCount(String as) {
    this.as = as;
  }

  @Override
  public String render(List<TableInstance> instances) {
    StringBuilder sb = new StringBuilder();
    if (as != null) {
      return "COUNT(*) as " + as;
    }
    return "COUNT(*)";
  }
}
