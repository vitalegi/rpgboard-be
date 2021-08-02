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
public class Asset {

  @Column(name = "asset_id")
  UUID assetId;

  @Column(name = "game_id")
  UUID gameId;

  @Column(name = "name")
  String name;

  @Column(name = "size")
  Long size;

  @Column(name = "content")
  String content;

  public static Map<String, Object> map(
      UUID assetId, UUID gameId, String name, Long size, String content) {
    Map<String, Object> map = new HashMap<>();
    map.put("asset_id", assetId);
    map.put("game_id", gameId);
    map.put("name", name);
    map.put("size", size);
    map.put("content", content);
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
