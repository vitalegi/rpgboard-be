package it.vitalegi.rpgboard.be.repository.querybuilder.pg.selectvalues;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.Field;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

public class SelectTableColumnValue implements Renderer {
  FieldsPicker picker;
  String as;

  public SelectTableColumnValue(FieldsPicker picker, String as) {
    this.picker = picker;
    this.as = as;
  }

  public SelectTableColumnValue(FieldsPicker picker) {
    this(picker, null);
  }

  @Override
  public String render(List<TableInstance> instances) {
    StringBuilder sb = new StringBuilder();
    return picker.fields(instances).stream()
        .map(field -> render(field, instances))
        .collect(Collectors.joining(", "));
  }

  protected String render(Field field, List<TableInstance> instances) {
    String alias = QueryTool.getAlias(field.getAlias(), instances);
    StringBuilder sb = new StringBuilder();
    if (StringUtil.isNotNullOrEmpty(alias)) {
      sb.append(alias).append(".");
    }
    sb.append(field.getField());
    if (StringUtil.isNotNullOrEmpty(as)) {
      sb.append(" as ").append(as);
    }
    return sb.toString();
  }
}
