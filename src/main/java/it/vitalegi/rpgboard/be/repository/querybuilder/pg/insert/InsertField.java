package it.vitalegi.rpgboard.be.repository.querybuilder.pg.insert;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.Field;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;

import java.util.List;
import java.util.stream.Collectors;

public class InsertField implements InsertRenderer {
  FieldsPicker picker;

  public InsertField(FieldsPicker picker) {
    this.picker = picker;
  }

  public String renderKeys(List<TableInstance> instances) {
    return picker.fields(instances).stream()
        .map(Field::getField)
        .collect(Collectors.joining(", ", "(", ")"));
  }

  @Override
  public String renderValues(List<TableInstance> instances) {
    return picker.fields(instances).stream()
        .map(field -> "#{" + field.getField() + "}")
        .collect(Collectors.joining(", ", "(", ")"));
  }
}
