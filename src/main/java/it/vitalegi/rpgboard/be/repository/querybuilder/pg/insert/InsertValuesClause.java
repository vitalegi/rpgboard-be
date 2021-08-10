package it.vitalegi.rpgboard.be.repository.querybuilder.pg.insert;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InsertValuesClause implements Renderer {

  protected List<InsertRenderer> renderers;

  public InsertValuesClause(List<InsertRenderer> renderers) {
    this.renderers = renderers;
  }
  public InsertValuesClause() {
    this(new ArrayList<>());
  }

  public InsertValuesClause all() {
    renderers.add(new InsertField(FieldsPicker.all()));
    return this;
  }

  public InsertValuesClause allExcept(List<String> except) {
    renderers.add(new InsertField(FieldsPicker.except(except)));
    return this;
  }

  public InsertValuesClause allExcept(String except) {
    renderers.add(new InsertField(FieldsPicker.except(Collections.singletonList(except))));
    return this;
  }

  @Override
  public String render(List<TableInstance> instances) {
    TableInstance instance = instances.get(0);
    return renderKeys(instances) + " VALUES " + renderValues(instances);
  }

  protected String renderKeys(List<TableInstance> instances) {
    return renderers.stream().map(r -> r.renderKeys(instances)).collect(Collectors.joining(", "));
  }

  protected String renderValues(List<TableInstance> instances) {
    return renderers.stream().map(r -> r.renderValues(instances)).collect(Collectors.joining(", "));
  }
}
