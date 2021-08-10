package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Table;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableFactory;

import java.util.HashMap;
import java.util.Map;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class User {
  public static final String USER_ID = "user_id";
  public static final String USERNAME = "username";
  public static final Table BUILDER =
      TableFactory.init()
          .tableName("RPG_User")
          .primaryKey(USER_ID)
          .fields(USER_ID, USERNAME)
          .build();

  @Column(name = USER_ID)
  String id;

  @Column(name = USERNAME)
  String name;

  public static Map<String, Object> map(String id, String name) {
    Map<String, Object> map = new HashMap<>();
    map.put(USER_ID, id);
    map.put(USERNAME, name);
    return map;
  }

  public static Map<String, Object> map(User entry) {
    return map(entry.getId(), entry.getName());
  }

  public static Map<String, Object> mapPK(String userId) {
    Map<String, Object> map = new HashMap<>();
    map.put(USER_ID, userId);
    return map;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    UserConverter.toJson(this, json);
    return json;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "User{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
  }
}
