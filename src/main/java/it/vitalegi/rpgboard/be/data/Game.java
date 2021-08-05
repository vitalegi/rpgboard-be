package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import it.vitalegi.rpgboard.be.util.PreparedStatementBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class Game {

  public static final String GAME_ID = "game_id";
  public static final String NAME = "name";
  public static final String OWNER_ID = "owner_id";
  public static final String IS_OPEN = "is_open";
  public static final PreparedStatementBuilder BUILDER =
      PreparedStatementBuilder.init()
          .tableName("RPG_Game")
          .fields(GAME_ID, NAME, OWNER_ID, IS_OPEN);

  @Column(name = GAME_ID)
  UUID id;

  @Column(name = NAME)
  String name;

  @Column(name = OWNER_ID)
  String ownerId;

  @Column(name = IS_OPEN)
  Boolean open;

  public static Map<String, Object> map(UUID id, String name, String ownerId, Boolean open) {
    Map<String, Object> map = new HashMap<>();
    map.put(GAME_ID, id);
    map.put(NAME, name);
    map.put(OWNER_ID, ownerId);
    map.put(IS_OPEN, open);
    return map;
  }

  public static Map<String, Object> map(Game game) {
    Map<String, Object> map = new HashMap<>();
    map.put(GAME_ID, game.getId());
    map.put(NAME, game.getName());
    map.put(OWNER_ID, game.getOwnerId());
    map.put(IS_OPEN, game.getOpen());
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

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public Boolean getOpen() {
    return open;
  }

  public void setOpen(Boolean open) {
    this.open = open;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    GameConverter.toJson(this, json);
    return json;
  }

  @Override
  public String toString() {
    return "Game{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", ownerId="
        + ownerId
        + ", open="
        + open
        + '}';
  }
}
