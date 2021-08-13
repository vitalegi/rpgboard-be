package it.vitalegi.rpgboard.be.repository.querybuilder.pg.updateset;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.QueryTool;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetClause implements Renderer {

  protected List<Renderer> renderers;

  public SetClause() {
    this.renderers = new ArrayList<>();
  }

  public static SetClause init() {
    return new SetClause();
  }

  public SetClause all(String alias) {
    renderers.add(new SetTableColumnValue(FieldsPicker.all(alias)));
    return this;
  }

  public SetClause exact(String field) {
    return exact(null, field);
  }

  public SetClause exact(String alias, String field) {
    renderers.add(
        new SetTableColumnValue(FieldsPicker.exact(alias, Collections.singletonList(field))));
    return this;
  }

  public SetClause exact(List<String> fields) {
    return exact(null, fields);
  }

  public SetClause exact(String alias, List<String> fields) {
    renderers.add(new SetTableColumnValue(FieldsPicker.exact(alias, fields)));
    return this;
  }

  public SetClause except(String alias, List<String> fields) {
    renderers.add(new SetTableColumnValue(FieldsPicker.except(alias, fields)));
    return this;
  }

  public SetClause except(List<String> fields) {
    renderers.add(new SetTableColumnValue(FieldsPicker.except(fields)));
    return except(null, fields);
  }

  @Override
  public String render(List<TableInstance> instances) {
    return QueryTool.renderer(instances, renderers, ", ", " SET ", "");
  }
}
