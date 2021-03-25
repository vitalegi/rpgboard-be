package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.templates.TupleMapper;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.ParametersMapped;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.util.HashMap;
import java.util.Map;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class Account {
  @Column(name = "account_id")
  String id;

  @Column(name = "name")
  String name;

  public static Map<String, Object> map(String id, String name) {
    Map<String,Object> map = new HashMap<>();
    map.put("account_id", id);
    map.put("name", name);
    return map;
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

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    AccountConverter.toJson(this, json);
    return json;
  }

  @Override
  public String toString() {
    return "Account [id=" + id + ", name=" + name + "]";
  }

}
