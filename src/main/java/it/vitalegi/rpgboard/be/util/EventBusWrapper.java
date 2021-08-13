package it.vitalegi.rpgboard.be.util;

import io.reactivex.Single;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import it.vitalegi.rpgboard.be.MainVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class EventBusWrapper {
  Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject EventBus eventBus;

  public Single<JsonObject> enterGame(UUID gameId, UUID userId) {
    return eventBus
        .rxRequest(
            "game.players.enter",
            new JsonObject().put("gameId", gameId.toString()).put("userId", userId.toString()),
            new DeliveryOptions().addHeader(MainVerticle.UID, userId.toString()))
        .map(message -> (JsonObject) message.body());
  }

  public Single<JsonObject> hasGrants(UUID gameId, UUID userId) {
    return eventBus
        .rxRequest(
            "game.players.hasGrants",
            new JsonObject().put("gameId", gameId.toString()).put("userId", userId.toString()),
            new DeliveryOptions().addHeader(MainVerticle.UID, userId.toString()))
        .map(message -> (JsonObject) message.body());
  }

  public void publishGamePlayerJoins(UUID gameId, UUID userId) {
    log.info("player {} joined {}", userId, gameId);

    eventBus.publish(
        "external.outgoing.game." + gameId + ".players",
        new JsonObject().put("action", "JOIN").put("userId", userId.toString()));
  }
}
