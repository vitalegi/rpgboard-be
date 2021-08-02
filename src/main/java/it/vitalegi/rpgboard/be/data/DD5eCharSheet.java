package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class DD5eCharSheet {

  @Column(name = "sheet_id")
  UUID sheetId;

  @Column(name = "game_id")
  UUID gameId;

  @Column(name = "owner_id")
  String ownerId;

  @Column(name = "content")
  JsonObject content;

  @Column(name = "last_update")
  OffsetDateTime lastUpdate;

  public static Map<String, Object> map(
      UUID sheetId, UUID gameId, String ownerId, JsonObject content, OffsetDateTime lastUpdate) {
    Map<String, Object> map = new HashMap<>();
    map.put("sheet_id", sheetId);
    map.put("game_id", gameId);
    map.put("owner_id", ownerId);
    map.put("content", content);
    map.put("last_update", lastUpdate);
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
