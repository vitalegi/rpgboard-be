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
public class GamePlayer {
  @Column(name = "game_id")
  UUID gameId;

  @Column(name = "user_id")
  String userId;

  @Column(name = "username")
  String username;

  public static Map<String, Object> map(UUID gameId, String userId, String username) {
    Map<String, Object> map = new HashMap<>();
    map.put("game_id", gameId);
    map.put("user_id", userId);
    map.put("username", username);
    return map;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    GamePlayerConverter.toJson(this, json);
    return json;
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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String toString() {
    return "GamePlayer{"
        + "gameId="
        + gameId
        + ", userId='"
        + userId
        + '\''
        + ", username='"
        + username
        + '\''
        + '}';
  }
}
