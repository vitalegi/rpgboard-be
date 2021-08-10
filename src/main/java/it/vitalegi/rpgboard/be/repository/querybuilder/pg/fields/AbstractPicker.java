package it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractPicker implements FieldsPicker {

  String alias;

  public AbstractPicker(String alias) {
    this.alias = alias;
  }

  @Override
  public List<Field> fields(List<TableInstance> tableInstances) {
    List<TableInstance> tables = QueryTool.getAllByAlias(alias, tableInstances);
    return tables.stream()
        .flatMap(
            table ->
                table.getFields().stream()
                    .filter(this::filter)
                    .map(field -> new Field(table.getAlias(), field)))
        .collect(Collectors.toList());
  }

  protected abstract boolean filter(String field);
}
