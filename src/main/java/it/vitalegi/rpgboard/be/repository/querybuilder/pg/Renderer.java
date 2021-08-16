package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import java.util.List;

public interface Renderer {
  String render(List<TableInstance> instances);
}
