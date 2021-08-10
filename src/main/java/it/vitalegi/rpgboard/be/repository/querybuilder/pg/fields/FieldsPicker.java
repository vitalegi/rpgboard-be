package it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields;

import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableInstance;

import java.util.Collections;
import java.util.List;

public interface FieldsPicker {
  public static FieldsPicker all(String alias) {
    return new All(alias);
  }

  public static FieldsPicker all() {
    return new All(null);
  }

  public static FieldsPicker exact(String alias, List<String> fields) {
    return new Exact(alias, fields);
  }

  public static FieldsPicker exact(List<String> fields) {
    return new Exact(null, fields);
  }

  public static FieldsPicker exact(String field) {
    return new Exact(null, Collections.singletonList(field));
  }

  public static FieldsPicker except(String alias, List<String> fields) {
    return new Except(alias, fields);
  }

  public static FieldsPicker except(List<String> fields) {
    return new Except(null, fields);
  }

  public List<Field> fields(List<TableInstance> tableInstances);
}
