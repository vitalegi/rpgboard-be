package it.vitalegi.rpgboard.be.repository;

import it.vitalegi.rpgboard.be.data.Asset;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class AssetRepository extends AbstractSinglePkCrudRepository<Asset, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public AssetRepository() {
    super(Mappers.ASSET, Asset::map, Asset::mapPK, Asset.BUILDER);
  }
}
