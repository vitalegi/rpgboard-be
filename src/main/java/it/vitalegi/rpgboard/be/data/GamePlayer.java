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
public class GamePlayer {
  public static final String GAME_ID = "game_id";
  public static final String USER_ID = "user_id";
  public static final String USERNAME = "username";
  public static final PreparedStatementBuilder BUILDER =
      PreparedStatementBuilder.init()
          .tableName("RPG_GamePlayer")
          .fields(GAME_ID, USER_ID, USERNAME);

  @Column(name = GAME_ID)
  UUID gameId;

  @Column(name = USER_ID)
  String userId;

  @Column(name = USERNAME)
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
