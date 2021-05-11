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
public class Board {
  @Column(name = "board_id")
  String boardId;

  @Column(name = "name")
  String name;

  public static Map<String, Object> map(String boardId, String name) {
    Map<String, Object> map = new HashMap<>();
    map.put("board_id", boardId);
    map.put("name", name);
    return map;
  }

  public String getId() {
    return boardId;
  }

  public void setId(String id) {
    this.boardId = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    BoardConverter.toJson(this, json);
    return json;
  }

  @Override
  public String toString() {
    return "Account [id=" + boardId + ", name=" + name + "]";
  }
}
