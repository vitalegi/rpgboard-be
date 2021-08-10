package it.vitalegi.rpgboard.be.repository.querybuilder.pg.returning;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.Field;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReturningClause implements Renderer {
  protected List<FieldsPicker> pickers;

  public ReturningClause(FieldsPicker... pickers) {
    this.pickers = Arrays.asList(pickers);
  }

  @Override
  public String render(List<TableInstance> instances) {
    String fields =
        pickers.stream()
            .flatMap(picker -> picker.fields(instances).stream())
            .map(field -> render(field, instances))
            .collect(Collectors.joining(", "));
    if (StringUtil.isNotNullOrEmpty(fields)) {
      return " RETURNING " + fields;
    }
    return "";
  }

  protected String render(Field field, List<TableInstance> instances) {
    return QueryTool.aliasDotName(field.getAlias(), field.getField(), instances);
  }
}
