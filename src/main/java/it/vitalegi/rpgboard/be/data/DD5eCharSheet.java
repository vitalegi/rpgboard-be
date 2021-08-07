package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.PreparedStatementFactory;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class DD5eCharSheet {
  public static final String SHEET_ID = "sheet_id";
  public static final String GAME_ID = "game_id";
  public static final String OWNER_ID = "owner_id";
  public static final String CONTENT = "content";
  public static final String LAST_UPDATE = "last_update";
  public static final PreparedStatementFactory BUILDER =
      PreparedStatementFactory.init()
          .tableName("RPG_DD5e_Sheet")
          .fields(SHEET_ID, GAME_ID, OWNER_ID, CONTENT, LAST_UPDATE);

  @Column(name = SHEET_ID)
  UUID sheetId;

  @Column(name = GAME_ID)
  UUID gameId;

  @Column(name = OWNER_ID)
  String ownerId;

  @Column(name = CONTENT)
  JsonObject content;

  @Column(name = LAST_UPDATE)
  OffsetDateTime lastUpdate;

  public static Map<String, Object> map(
      UUID sheetId, UUID gameId, String ownerId, JsonObject content, OffsetDateTime lastUpdate) {
    Map<String, Object> map = new HashMap<>();
    map.put(SHEET_ID, sheetId);
    map.put(GAME_ID, gameId);
    map.put(OWNER_ID, ownerId);
    map.put(CONTENT, content);
    map.put(LAST_UPDATE, lastUpdate);
    return map;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    DD5eCharSheetConverter.toJson(this, json);
    return json;
  }

  @Override
  public String toString() {
    return "DD5eCharSheet{"
        + "sheetId="
        + sheetId
        + ", gameId="
        + gameId
        + ", ownerId='"
        + ownerId
        + '\''
        + ", content="
        + content
        + ", lastUpdate="
        + lastUpdate
        + '}';
  }

  public UUID getSheetId() {
    return sheetId;
  }

  public void setSheetId(UUID sheetId) {
    this.sheetId = sheetId;
  }

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public JsonObject getContent() {
    return content;
  }

  public void setContent(JsonObject content) {
    this.content = content;
  }

  public OffsetDateTime getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(OffsetDateTime lastUpdate) {
    this.lastUpdate = lastUpdate;
  }
}
