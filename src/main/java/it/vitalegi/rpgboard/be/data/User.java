package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.util.HashMap;
import java.util.Map;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class User {
  @Column(name = "user_id")
  String id;

  @Column(name = "username")
  String name;

  public static Map<String, Object> map(String id, String name) {
    Map<String, Object> map = new HashMap<>();
    map.put("user_id", id);
    map.put("username", name);
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
