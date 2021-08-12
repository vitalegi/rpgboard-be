package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.constants.VisibilityPolicy;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.data.GamePlayerRole;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.SelectFactory;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.from.FromClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.from.Join;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.from.JoinType;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.selectvalues.SelectedValues;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsPlaceholder;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsStringValue;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class GameRepository extends AbstractSinglePkCrudRepository<Game, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public GameRepository() {
    super(Mappers.GAME, Game::map, Game::mapPK, Game.BUILDER);
  }

  public Single<List<JsonObject>> getAvailableGames(SqlConnection connection, UUID userId) {

    Map<String, Object> entry = new HashMap<>();
    entry.put(GamePlayerRole.USER_ID, userId);

    return queryList(
        connection,
        SelectFactory.init(Game.BUILDER, "g")
            .table(GamePlayerRole.BUILDER, "gpr")
            .values(new SelectedValues().distinct(true).all("g"))
            .from(
                new FromClause("g")
                    .join(
                        new Join(JoinType.LEFT_JOIN, "gpr")
                            .and("g", Game.GAME_ID, "gpr", GamePlayerRole.GAME_ID)))
            .where(
                WhereClause.or(
                    // user is member of the game
                    new EqualsPlaceholder(FieldsPicker.exact("gpr", GamePlayerRole.USER_ID)),
                    // game is public
                    new EqualsStringValue(
                        FieldsPicker.exact("g", Game.VISIBILITY_POLICY), VisibilityPolicy.PUBLIC)))
            .build(),
        entry,
        customRowMapper(
            row -> {
              Game game = Mappers.GAME.getDelegate().map(row);
              return JsonObject.mapFrom(game);
            }));
  }
}
