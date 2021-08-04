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
public class Asset {
  public static final String ASSET_ID = "asset_id";
  public static final String GAME_ID = "game_id";
  public static final String NAME = "name";
  public static final String SIZE = "size";
  public static final String CONTENT = "content";
  public static final PreparedStatementBuilder BUILDER =
      PreparedStatementBuilder.init()
          .tableName("RPG_Asset")
          .fields(ASSET_ID, GAME_ID, NAME, SIZE, CONTENT);

  @Column(name = ASSET_ID)
  UUID assetId;

  @Column(name = GAME_ID)
  UUID gameId;

  @Column(name = NAME)
  String name;

  @Column(name = SIZE)
  Long size;

  @Column(name = CONTENT)
  String content;

  public static Map<String, Object> map(
      UUID assetId, UUID gameId, String name, Long size, String content) {
    Map<String, Object> map = new HashMap<>();
    map.put(ASSET_ID, assetId);
    map.put(GAME_ID, gameId);
    map.put(NAME, name);
    map.put(SIZE, size);
    map.put(CONTENT, content);
    return map;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    AssetConverter.toJson(this, json);
    return json;
  }

  public UUID getAssetId() {
    return assetId;
  }

  public void setAssetId(UUID assetId) {
    this.assetId = assetId;
  }

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "Asset{"
        + "assetId="
        + assetId
        + ", gameId="
        + gameId
        + ", name='"
        + name
        + '\''
        + ", size="
        + size
        + ", content='"
        + content
        + '\''
        + '}';
  }
}
