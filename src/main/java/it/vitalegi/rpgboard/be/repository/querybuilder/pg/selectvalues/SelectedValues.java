package it.vitalegi.rpgboard.be.repository.querybuilder.pg.selectvalues;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Renderer;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SelectedValues implements Renderer {

  protected boolean distinct;
  protected List<Renderer> renderers;

  public SelectedValues() {
    this.renderers = new ArrayList<>();
  }

  public SelectedValues distinct(boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  public SelectedValues all(String alias) {
    renderers.add(new SelectTableColumnValue(FieldsPicker.all(alias), null));
    return this;
  }

  public SelectedValues all() {
    renderers.add(new SelectTableColumnValue(FieldsPicker.all(), null));
    return this;
  }

  public SelectedValues exact(String alias, String field, String as) {
    renderers.add(
        new SelectTableColumnValue(
            FieldsPicker.exact(alias, Collections.singletonList(field)), as));
    return this;
  }

  public SelectedValues exact(String alias, List<String> fields) {
    renderers.add(new SelectTableColumnValue(FieldsPicker.exact(alias, fields)));
    return this;
  }

  public SelectedValues exact(String alias, String field) {
    renderers.add(
        new SelectTableColumnValue(FieldsPicker.exact(alias, Collections.singletonList(field))));
    return this;
  }

  public SelectedValues exact(List<String> fields) {
    renderers.add(new SelectTableColumnValue(FieldsPicker.exact(fields)));
    return this;
  }

  public SelectedValues except(String alias, List<String> fields) {
    renderers.add(new SelectTableColumnValue(FieldsPicker.except(alias, fields)));
    return this;
  }

  public SelectedValues except(String alias, String field) {
    renderers.add(
        new SelectTableColumnValue(FieldsPicker.except(alias, Collections.singletonList(field))));
    return this;
  }

  public SelectedValues except(List<String> fields) {
    renderers.add(new SelectTableColumnValue(FieldsPicker.except(null, fields)));
    return this;
  }

  public SelectedValues except(String field) {
    renderers.add(
        new SelectTableColumnValue(FieldsPicker.except(null, Collections.singletonList(field))));
    return this;
  }

  public SelectedValues count(String as) {
    renderers.add(new SelectCount(as));
    return this;
  }

  @Override
  public String render(List<TableInstance> instances) {
    return renderers.stream()
        .map(r -> r.render(instances))
        .filter(StringUtil::isNotNullOrEmpty)
        .collect(Collectors.joining(", ", distinctClause(), ""));
  }

  protected String distinctClause() {
    if (distinct) {
      return "DISTINCT ";
    }
    return "";
  }
}
