package it.vitalegi.rpgboard.be.repository.querybuilder.pg.insert;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;

import java.util.List;

public interface InsertRenderer {

  String renderKeys(List<TableInstance> instances);

  String renderValues(List<TableInstance> instances);
}
