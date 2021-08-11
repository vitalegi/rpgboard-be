package it.vitalegi.rpgboard.be.security;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.web.handler.sockjs.BridgeEvent;
import it.vitalegi.rpgboard.be.MainVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

@Singleton
public class WebSocketBridgeListener implements Handler<BridgeEvent> {
  Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject AuthProvider authProvider;
  @Inject EventBus eventBus;

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
    getUser(event.getRawMessage())
        .subscribe(
            user -> {
              String externalUserId = user.principal().getString(MainVerticle.EXTERNAL_UID);
              log.info("WebSocket > {}", user.principal());
              if (isAuthenticated(externalUserId)) {
                // TODO recupero userId a partire da externalUserId e print
                // logDetails(log::debug, event, true, userId, null, "");
                logDetails(log::debug, event, true, null, null, "");
                event.getRawMessage().getJsonObject("headers").put(MainVerticle.UID, "TODO");
                event
                    .getRawMessage()
                    .getJsonObject("headers")
                    .put(MainVerticle.EXTERNAL_UID, externalUserId);
                event.complete(true);

              } else {
                // TODO recupero userId a partire da externalUserId e print
                // logDetails(log::debug, event, false, userId, null, "");
                logDetails(log::debug, event, false, null, null, "");
                event.complete(false);
              }
            },
            e -> {
              log.error("Failed", e);
              event.complete(false);
            });
  }

  protected void processRegistration(BridgeEvent event) {
    getUser(event.getRawMessage())
        .subscribe(
            user -> {
              String externalUserId = user.principal().getString(MainVerticle.EXTERNAL_UID);
              if (!isAuthenticated(externalUserId)) {
                // TODO recupero userId a partire da externalUserId e print
                logDetails(log::error, event, false, null, null, "User not logged in");
                event.complete(false);
                return;
              }
              UUID gameId = getGameId(event);
              if (gameId == null) {
                // TODO recupero userId a partire da externalUserId e print
                logDetails(
                    log::info,
                    event,
                    true,
                    null,
                    null,
                    "Address not connected to a game, continue");
                event.complete(true);
                return;
              }
              log.debug(
                  "Address {} connected to game {}, check if user has permission",
                  getAddress(event),
                  gameId);
              // TODO recupero userId a partire da externalUserId e print
              processGameRegistration(event, gameId, null);
            },
            e -> {
              log.error("Failed", e);
              event.complete(false);
            });
  }

  protected void processGenericRequest(BridgeEvent event) {
    logDetails(log::debug, event, true, null, null, "");
    event.complete(true);
  }

  protected void processGameRegistration(BridgeEvent event, UUID gameId, UUID userId) {
    eventBus
        .rxRequest("game.userRoles.get", new JsonObject())
        .map(msg -> (JsonObject) msg.body())
        .subscribe(
            roles -> {
              log.info("RECEIVED: {}", roles);
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
      UUID userId,
      UUID gameId,
      String msg) {

    logDetails(log, event, accept, userId, gameId, msg, null);
  }

  protected void logDetails(
      BiConsumer<String, Object[]> log,
      BridgeEvent event,
      boolean accept,
      UUID userId,
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

  protected Single<User> getUser(JsonObject obj) {
    String token = null;
    try {
      token = obj.getJsonObject("headers", new JsonObject()).getString("Authorization", null);
      return authProvider.getUser(token);
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
}
