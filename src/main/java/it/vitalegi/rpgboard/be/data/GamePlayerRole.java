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
public class GamePlayerRole {
  @Column(name = "entry_id")
  Long entryId;

  @Column(name = "game_id")
  UUID gameId;

  @Column(name = "user_id")
  String userId;

  @Column(name = "role")
  String role;

  public static Map<String, Object> map(Long entryId, UUID gameId, String userId, String role) {
    Map<String, Object> map = new HashMap<>();
    map.put("entry_id", entryId);
    map.put("game_id", gameId);
    map.put("user_id", userId);
    map.put("role", role);
    return map;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    GamePlayerRoleConverter.toJson(this, json);
    return json;
  }

  public Long getEntryId() {
    return entryId;
  }

  public void setEntryId(Long entryId) {
    this.entryId = entryId;
  }

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return "GamePlayerRole{"
        + "entryId="
        + entryId
        + ", gameId="
        + gameId
        + ", userId='"
        + userId
        + '\''
        + ", role='"
        + role
        + '\''
        + '}';
  }
}
