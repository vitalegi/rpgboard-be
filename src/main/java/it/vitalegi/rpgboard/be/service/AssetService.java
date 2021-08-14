package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Asset;
import it.vitalegi.rpgboard.be.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.Base64;
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
    asset.setContent(getFile(content));
    asset.setContentType(getContentType(content));
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
        .getByIdNoContent(conn, assetId)
        .flatMap(
            asset ->
                gameService
                    .checkGrantGameRead(conn, asset.getGameId(), userId)
                    .map(hasGrant -> asset));
  }

  public Single<Asset> getAssetContent(SqlConnection conn, UUID assetId) {
    notNull(assetId, "assetId null");

    return assetRepository.getById(conn, assetId);
  }

  public Single<List<Asset>> getAssets(SqlConnection conn, UUID gameId, UUID userId) {
    notNull(gameId, "assetId null");

    return gameService
        .checkGrantGameRead(conn, gameId, userId)
        .flatMap(hasGrant -> assetRepository.getByGameIdNoContent(conn, gameId));
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

  protected Buffer getFile(String content) {
    String file = content.substring(content.indexOf(',') + 1);
    byte[] decoded = Base64.getDecoder().decode(file);
    return Buffer.buffer(decoded);
  }

  protected String getContentType(String content) {
    String metadata = content.substring(0, content.indexOf(','));
    metadata = metadata.split(";")[0].replace("data:", "");
    return metadata;
  }

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }
}
