package it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;

import java.util.Collections;
import java.util.List;

public interface FieldsPicker {
  static FieldsPicker all(String alias) {
    return new All(alias);
  }

  static FieldsPicker all() {
    return new All(null);
  }

  static FieldsPicker exact(String alias, List<String> fields) {
    return new Exact(alias, fields);
  }

  static FieldsPicker exact(String alias, String field) {
    return new Exact(alias, Collections.singletonList(field));
  }

  static FieldsPicker exact(List<String> fields) {
    return new Exact(null, fields);
  }

  static FieldsPicker exact(String field) {
    return new Exact(null, Collections.singletonList(field));
  }

  static FieldsPicker except(String alias, List<String> fields) {
    return new Except(alias, fields);
  }

  static FieldsPicker except(List<String> fields) {
    return new Except(null, fields);
  }

  List<Field> fields(List<TableInstance> tableInstances);
}
