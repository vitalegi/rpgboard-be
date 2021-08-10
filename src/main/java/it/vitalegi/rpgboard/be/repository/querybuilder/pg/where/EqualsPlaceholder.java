package it.vitalegi.rpgboard.be.repository.querybuilder.pg.where;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

public class EqualsPlaceholder implements Renderer {
  String placeholder;
  FieldsPicker picker;

  public EqualsPlaceholder(FieldsPicker picker) {
    this.picker = picker;
  }

  public EqualsPlaceholder(FieldsPicker picker, String placeholder) {
    this.picker = picker;
    this.placeholder = placeholder;
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

    if (StringUtil.isNotNullOrEmpty(placeholder)) {
      sb.append("#{").append(placeholder).append("}");
    } else {
      sb.append("#{").append(field).append("}");
    }
    return sb.toString();
  }
}
