package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.model.AllowAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class GamePlayerRoleService {
  @Inject GamePlayerRoleServiceLocal gamePlayerRoleServiceLocal;

  @Inject EventBus eventBus;

  Logger log = LoggerFactory.getLogger(GamePlayerRoleService.class);

  public Single<AllowAction> enterGame(SqlConnection conn, UUID gameId, UUID userId) {
    return gamePlayerRoleServiceLocal
        .hasUserRoles(conn, gameId, userId)
        .map(this::allowAction)
        .map(
            allow -> {
              log.info("User {} joined game {}. Notify all players", userId, gameId);
              return allow;
            });
  }

  public Single<AllowAction> hasUserGrants(SqlConnection conn, UUID gameId, UUID userId) {
    return gamePlayerRoleServiceLocal.hasUserRoles(conn, gameId, userId).map(this::allowAction);
  }

  protected AllowAction allowAction(Boolean allowed) {
    AllowAction allow = new AllowAction();
    allow.setAllowed(allowed);
    return allow;
  }
}
