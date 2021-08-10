package it.vitalegi.rpgboard.be.repository.querybuilder.pg.updateset;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;

import java.util.List;
import java.util.stream.Collectors;

public class SetTableColumnValue implements Renderer {
  FieldsPicker picker;

  public SetTableColumnValue(FieldsPicker picker) {
    this.picker = picker;
  }

  @Override
  public String render(List<TableInstance> instances) {
    return picker.fields(instances).stream()
        .map(
            field ->
                QueryTool.aliasDotName(field.getAlias(), field.getField(), instances)
                    + "=#{"
                    + field.getField()
                    + "}")
        .collect(Collectors.joining(", "));
  }
}
