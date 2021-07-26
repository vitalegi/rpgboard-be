package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class Game {
  @Column(name = "game_id")
  UUID id;

  @Column(name = "name")
  String name;

  public static Map<String, Object> map(Long id, String name) {
    Map<String, Object> map = new HashMap<>();
    map.put("game_id", id);
    map.put("name", name);
    return map;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
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
    GameConverter.toJson(this, json);
    return json;
  }

  @Override
  public String toString() {
    return "Game [id=" + id + ", name=" + name + "]";
  }
}
