package it.vitalegi.rpgboard.be.repository.querybuilder.pg.where;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;

import java.util.List;
import java.util.stream.Collectors;

public class EqualsStringValue implements Renderer {
  String value;
  FieldsPicker picker;

  public EqualsStringValue(FieldsPicker picker, String value) {
    this.picker = picker;
    this.value = value;
  }

  @Override
  public String render(List<TableInstance> instances) {
    return picker.fields(instances).stream()
        .map(f -> render(f.getAlias(), f.getField(), instances))
        .collect(Collectors.joining(" AND "));
  }

  public String render(String alias, String field, List<TableInstance> instances) {
    StringBuilder sb = new StringBuilder();
    sb.append(QueryTool.aliasDotName(alias, field, instances));
    sb.append("=");

    sb.append("'").append(value).append("'");
    return sb.toString();
  }
}
