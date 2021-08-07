package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.PreparedStatementFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class GamePlayerRole {
  public static final String ENTRY_ID = "entry_id";
  public static final String GAME_ID = "game_id";
  public static final String USER_ID = "user_id";
  public static final String ROLE = "role";
  public static final PreparedStatementFactory BUILDER =
      PreparedStatementFactory.init()
          .tableName("RPG_GamePlayerRole")
          .fields(ENTRY_ID, GAME_ID, USER_ID, ROLE);

  @Column(name = ENTRY_ID)
  Long entryId;

  @Column(name = GAME_ID)
  UUID gameId;

  @Column(name = USER_ID)
  String userId;

  @Column(name = ROLE)
  String role;

  public static Map<String, Object> map(Long entryId, UUID gameId, String userId, String role) {
    Map<String, Object> map = new HashMap<>();
    map.put(ENTRY_ID, entryId);
    map.put(GAME_ID, gameId);
    map.put(USER_ID, userId);
    map.put(ROLE, role);
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
