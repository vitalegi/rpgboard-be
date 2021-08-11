package it.vitalegi.rpgboard.be.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.Table;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.TableFactory;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DataObject(generateConverter = true)
@RowMapped(formatter = SnakeCase.class)
public class Game {

  public static final String GAME_ID = "game_id";
  public static final String NAME = "name";
  public static final String OWNER_ID = "owner_id";
  public static final String STATUS = "status";
  public static final String TYPE = "type";
  public static final String VISIBILITY_POLICY = "visibility_policy";
  public static final String CREATE_DATE = "create_date";
  public static final String LAST_UPDATE_DATE = "last_update";

  public static final Table BUILDER =
      TableFactory.init()
          .tableName("RPG_Game")
          .primaryKey(GAME_ID)
          .autoGenerated(GAME_ID)
          .fields(
              GAME_ID,
              NAME,
              OWNER_ID,
              STATUS,
              TYPE,
              VISIBILITY_POLICY,
              CREATE_DATE,
              LAST_UPDATE_DATE)
          .build();

  @Column(name = GAME_ID)
  UUID gameId;

  @Column(name = NAME)
  String name;

  @Column(name = OWNER_ID)
  UUID ownerId;

  @Column(name = VISIBILITY_POLICY)
  String visibilityPolicy;

  @Column(name = TYPE)
  String type;

  @Column(name = STATUS)
  String status;

  @Column(name = CREATE_DATE)
  OffsetDateTime createDate;

  @Column(name = LAST_UPDATE_DATE)
  OffsetDateTime lastUpdate;

  public static Map<String, Object> map(
      UUID gameId,
      String name,
      UUID ownerId,
      String status,
      String type,
      String visibilityPolicy,
      OffsetDateTime createDate,
      OffsetDateTime lastUpdate) {
    Map<String, Object> map = new HashMap<>();
    map.put(GAME_ID, gameId);
    map.put(NAME, name);
    map.put(OWNER_ID, ownerId);
    map.put(STATUS, status);
    map.put(TYPE, type);
    map.put(VISIBILITY_POLICY, visibilityPolicy);
    map.put(CREATE_DATE, createDate);
    map.put(LAST_UPDATE_DATE, lastUpdate);
    return map;
  }

  public static Map<String, Object> map(Game entry) {
    return map(
        entry.getGameId(),
        entry.getName(),
        entry.getOwnerId(),
        entry.getStatus(),
        entry.getType(),
        entry.getVisibilityPolicy(),
        entry.getCreateDate(),
        entry.getLastUpdate());
  }

  public static Map<String, Object> mapPK(UUID gameId) {
    Map<String, Object> map = new HashMap<>();
    map.put(GAME_ID, gameId);
    return map;
  }

  @Override
  public String toString() {
    return "Game{"
        + "gameId="
        + gameId
        + ", name='"
        + name
        + '\''
        + ", ownerId="
        + ownerId
        + ", visibilityPolicy='"
        + visibilityPolicy
        + '\''
        + ", type='"
        + type
        + '\''
        + ", status='"
        + status
        + '\''
        + ", createDate="
        + createDate
        + ", lastUpdate="
        + lastUpdate
        + '}';
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

  public UUID getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(UUID ownerId) {
    this.ownerId = ownerId;
  }

  public String getVisibilityPolicy() {
    return visibilityPolicy;
  }

  public void setVisibilityPolicy(String visibilityPolicy) {
    this.visibilityPolicy = visibilityPolicy;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public OffsetDateTime getCreateDate() {
    return createDate;
  }

  public void setCreateDate(OffsetDateTime createDate) {
    this.createDate = createDate;
  }

  public OffsetDateTime getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(OffsetDateTime lastUpdate) {
    this.lastUpdate = lastUpdate;
  }
}
