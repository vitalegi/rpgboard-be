package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Asset;
import it.vitalegi.rpgboard.be.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Singleton
public class AssetService {
  @Inject protected AssetRepository assetRepository;
  @Inject protected GameService gameService;

  Logger log = LoggerFactory.getLogger(this.getClass());

  public Single<Asset> addAsset(
      SqlConnection conn,
      UUID gameId,
      UUID userId,
      String name,
      String content,
      JsonObject metadata) {
    notNull(userId, "userId null");
    notNull(gameId, "gameId null");
    notNull(name, "name null");
    Asset asset = new Asset();
    asset.setName(name);
    asset.setUserId(userId);
    asset.setSize((long) content.length());
    asset.setContent(content);
    asset.setGameId(gameId);
    asset.setMetadata(metadata);
    asset.setSize((long) content.length());
    OffsetDateTime now = OffsetDateTime.now();
    asset.setCreateDate(now);
    asset.setLastUpdate(now);

    return gameService
        .checkGrantGameWrite(conn, gameId, userId)
        .flatMap(b -> assetRepository.add(conn, asset).singleOrError());
  }

  public Single<Asset> getAsset(SqlConnection conn, UUID assetId, UUID userId) {
    notNull(assetId, "assetId null");

    return assetRepository
        .getById(conn, assetId)
        .flatMap(
            asset ->
                gameService
                    .checkGrantGameRead(conn, asset.getGameId(), userId)
                    .map(hasGrant -> asset));
  }

  public Single<List<Asset>> getAssets(SqlConnection conn, UUID gameId, UUID userId) {
    notNull(gameId, "assetId null");

    return gameService
        .checkGrantGameRead(conn, gameId, userId)
        .flatMap(hasGrant -> assetRepository.getByGameId(conn, gameId));
  }

  public Single<Asset> deleteAsset(SqlConnection conn, UUID assetId, UUID userId) {
    notNull(userId, "userId null");

    return assetRepository
        .getById(conn, assetId)
        .flatMap(
            asset ->
                gameService
                    .checkGrantGameWrite(conn, asset.getGameId(), userId)
                    .map(hasGrant -> asset))
        .flatMap(asset -> assetRepository.delete(conn, asset.getAssetId()).singleOrError());
  }

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }
}
