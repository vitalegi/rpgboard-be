package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Asset;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.SelectFactory;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.selectvalues.SelectedValues;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsPlaceholder;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class AssetRepository extends AbstractSinglePkCrudRepository<Asset, UUID> {

  protected static final SelectedValues NO_CONTENT = new SelectedValues().except(Asset.CONTENT);
  Logger log = LoggerFactory.getLogger(this.getClass());

  public AssetRepository() {
    super(Mappers.ASSET, Asset::map, Asset::mapPK, Asset.BUILDER);
  }

  public Single<List<Asset>> getByGameIdNoContent(SqlConnection connection, UUID gameId) {
    Map<String, Object> in = new HashMap<>();
    in.put(Asset.GAME_ID, gameId);
    return executeQuery(
            connection,
            SelectFactory.init(table)
                .values(NO_CONTENT)
                .where(WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(Asset.GAME_ID))))
                .build(),
            in)
        .toList();
  }

  public Single<Asset> getByIdNoContent(SqlConnection connection, UUID assetId) {
    Map<String, Object> in = new HashMap<>();
    in.put(Asset.ASSET_ID, assetId);
    return executeQuery(
            connection,
            SelectFactory.init(table)
                .values(NO_CONTENT)
                .where(WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact(Asset.ASSET_ID))))
                .build(),
            in)
        .singleOrError();
  }
}
