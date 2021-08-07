package it.vitalegi.rpgboard.be.repository;

import it.vitalegi.rpgboard.be.data.DD5eCharSheet;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class DD5eCharSheetRepository extends AbstractSinglePkCrudRepository<DD5eCharSheet, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public DD5eCharSheetRepository() {
    super(Mappers.DD_5_E_CHAR_SHEET, DD5eCharSheet::map, DD5eCharSheet::mapPK, DD5eCharSheet.BUILDER);
  }
}
