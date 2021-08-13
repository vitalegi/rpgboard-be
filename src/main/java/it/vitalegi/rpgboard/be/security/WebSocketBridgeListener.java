package it.vitalegi.rpgboard.be.security;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.web.handler.sockjs.BridgeEvent;
import it.vitalegi.rpgboard.be.MainVerticle;
import it.vitalegi.rpgboard.be.util.EventBusWrapper;
import it.vitalegi.rpgboard.be.util.TopicUtil;
import it.vitalegi.rpgboard.be.util.UuidUtil;
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
  @Inject EventBusWrapper eventBus;
  @Inject TopicUtil topicUtil;

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
        processTopicRegistration(event);
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

  protected Single<Boolean> processAuth(BridgeEvent event) {
    return getUser(event)
        .map(
            user -> {
              UUID userId = UuidUtil.getUUID(user.principal().getString(MainVerticle.UID));
              String externalUserId = user.principal().getString(MainVerticle.EXTERNAL_UID);
              if (isAuthenticated(userId)) {
                event
                    .getRawMessage()
                    .getJsonObject("headers")
                    .put(MainVerticle.UID, userId)
                    .put(MainVerticle.EXTERNAL_UID, externalUserId);
                logDetails(
                    log::debug, event, true, userId, null, "externalUserId=" + externalUserId);
                return true;
              } else {
                logDetails(
                    log::info, event, false, userId, null, "externalUserId=" + externalUserId);
                return false;
              }
            });
  }

  protected void processIncomingMessage(BridgeEvent event) {
    processAuth(event).subscribe(event::complete);
  }

  protected void processTopicRegistration(BridgeEvent event) {
    processAuth(event)
        .subscribe(
            accept -> {
              if (!accept) {
                log.info("Not registered");
                event.complete(false);
                return;
              }
              UUID gameId = topicUtil.getGameId(getAddress(event));
              if (gameId == null) {
                log.debug("Address {} not connected to a game, continue", getAddress(event));
                event.complete(true);
                return;
              }
              log.debug(
                  "Address {} connected to game {}, check if user has permission",
                  getAddress(event),
                  gameId);

              UUID userId =
                  UuidUtil.getUUID(
                      event.getRawMessage().getJsonObject("headers").getString(MainVerticle.UID));

              processGameTopicRegistration(event, gameId, userId);
            });
  }

  protected void processGenericRequest(BridgeEvent event) {
    logDetails(log::debug, event, true, null, null, "");
    event.complete(true);
  }

  protected void processGameTopicRegistration(BridgeEvent event, UUID gameId, UUID userId) {
    log.debug("game registration gameId={} userId={}", gameId, userId);
    eventBus
        .hasGrants(gameId, userId)
        .subscribe(
            obj -> {
              logDetails(log::debug, event, true, userId, gameId, "GamePlayerRole check");
              event.complete(obj.getBoolean("allowed"));
              if (topicUtil.isGamePlayerTopic(getAddress(event))) {
                eventBus.publishGamePlayerJoins(gameId, userId);
              }
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

  protected Single<User> getUser(BridgeEvent event) {
    String token =
        event
            .getRawMessage()
            .getJsonObject("headers", new JsonObject())
            .getString("Authorization", null);
    return authProvider.getUser(token);
  }

  protected boolean isAuthenticated(UUID userId) {
    return userId != null;
  }

  protected String getAddress(BridgeEvent event) {
    if (event != null && event.getRawMessage() != null) {
      return event.getRawMessage().getString("address");
    }
    return null;
  }
}
