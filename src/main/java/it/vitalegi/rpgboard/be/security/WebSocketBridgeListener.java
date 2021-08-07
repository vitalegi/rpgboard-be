package it.vitalegi.rpgboard.be.security;

import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.reactivex.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.MainVerticle;
import it.vitalegi.rpgboard.be.service.GamePlayerRoleServiceLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class WebSocketBridgeListener implements Handler<BridgeEvent> {
  Logger log = LoggerFactory.getLogger(this.getClass());
  AuthProvider authProvider;
  GamePlayerRoleServiceLocal gamePlayerRoleServiceLocal;
  PgPool pgPool;

  public WebSocketBridgeListener(
      AuthProvider authProvider,
      GamePlayerRoleServiceLocal gamePlayerRoleServiceLocal,
      PgPool pgPool) {
    this.authProvider = authProvider;
    this.gamePlayerRoleServiceLocal = gamePlayerRoleServiceLocal;
    this.pgPool = pgPool;
  }

  @Override
  public void handle(BridgeEvent event) {
    try {
      if (BridgeEventType.SOCKET_CREATED == event.type()) {
        processSocketCreation(event);
        return;
      }
      if (Arrays.asList(BridgeEventType.PUBLISH, BridgeEventType.SEND).contains(event.type())) {
        processIncomingMessage(event);
        return;
      }
      if (BridgeEventType.REGISTER == event.type()) {
        processRegistration(event);
        return;
      }
      processGenericRequest(event);
    } catch (Throwable e) {
      log.error(
          "WEB_SOCKET type={}, id={}, address={}",
          event.type(),
          event.socket().webSession().id(),
          getAddress(event),
          e);
      event.complete(false);
    }
  }

  protected void processSocketCreation(BridgeEvent event) {
    logDetails(log::info, event, true, null, null, "");
    event.complete(true);
  }

  protected void processIncomingMessage(BridgeEvent event) {
    String userId = getUserId(event.getRawMessage());
    if (isAuthenticated(userId)) {
      logDetails(log::debug, event, true, userId, null, "");
      event.getRawMessage().getJsonObject("headers").put(MainVerticle.UID, userId);
      event.complete(true);
    } else {
      logDetails(log::debug, event, false, userId, null, "");
      event.complete(false);
    }
  }

  protected void processRegistration(BridgeEvent event) {
    String userId = getUserId(event.getRawMessage());
    if (!isAuthenticated(userId)) {
      logDetails(log::error, event, false, userId, null, "User not logged in");
      event.complete(false);
      return;
    }
    UUID gameId = getGameId(event);
    if (gameId == null) {
      logDetails(log::info, event, true, userId, null, "Address not connected to a game, continue");
      event.complete(true);
      return;
    }
    log.debug(
        "Address {} connected to game {}, check if user has permission", getAddress(event), gameId);
    processGameRegistration(event, gameId, userId);
  }

  protected void processGenericRequest(BridgeEvent event) {
    logDetails(log::debug, event, true, null, null, "");
    event.complete(true);
  }

  protected void processGameRegistration(BridgeEvent event, UUID gameId, String userId) {
    cx(conn -> gamePlayerRoleServiceLocal.getUserRoles(conn, gameId, userId).toMaybe())
        .subscribe(
            roles -> {
              boolean hasRoles = roles != null && !roles.isEmpty();
              logDetails(log::info, event, hasRoles, userId, gameId, "GamePlayerRole check");
              event.complete(hasRoles);
            },
            e -> {
              logDetails(log::error, event, false, userId, gameId, e.getMessage(), e);
              event.complete(false);
            });
  }

  protected void logDetails(
      BiConsumer<String, Object[]> log,
      BridgeEvent event,
      boolean accept,
      String userId,
      UUID gameId,
      String msg) {

    logDetails(log, event, accept, userId, gameId, msg, null);
  }

  protected void logDetails(
      BiConsumer<String, Object[]> log,
      BridgeEvent event,
      boolean accept,
      String userId,
      UUID gameId,
      String msg,
      Throwable e) {

    List<Object> values =
        Arrays.asList(
            event.type(),
            event.socket().webSession().id(),
            getAddress(event),
            accept,
            userId,
            gameId,
            msg);
    log.accept(
        "WEB_SOCKET type={}, id={}, address={}, accepted={}, userId={}, gameId={}, msg={}",
        values.toArray());
  }

  protected String getUserId(JsonObject obj) {
    String token = null;
    try {
      token = obj.getJsonObject("headers", new JsonObject()).getString("Authorization", null);
      return authProvider.getUser(token).principal().getString(MainVerticle.UID);
    } catch (Exception e) {
      log.error("Error validating the request for token {}, {}", token, e.getMessage());
      return null;
    }
  }

  protected boolean isAuthenticated(String userId) {
    return userId != null;
  }

  protected String getAddress(BridgeEvent event) {
    if (event != null && event.getRawMessage() != null) {
      return event.getRawMessage().getString("address");
    }
    return null;
  }

  protected UUID getGameId(BridgeEvent event) {
    String address = getAddress(event);
    if (address == null) {
      return null;
    }
    if (address.startsWith("external.outgoing.game.")) {
      String subaddress = address.substring("external.outgoing.game.".length());
      String gameId = subaddress;
      if (subaddress.contains(".")) {
        gameId = subaddress.substring(0, subaddress.indexOf("."));
      }
      return UUID.fromString(gameId);
    }
    return null;
  }

  protected <T> Maybe<T> cx(Function<SqlConnection, Maybe<T>> function) {
    return pgPool.rxWithConnection(function);
  }
}
