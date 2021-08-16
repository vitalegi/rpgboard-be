package it.vitalegi.rpgboard.be.service;

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
public class EventBusService {
  Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject EventBus eventBus;

  public Single<Boolean> joinGame(UUID gameId, UUID userId) {
    return eventBus
        .rxRequest(
            "game.join",
            new JsonObject().put("gameId", gameId.toString()).put("userId", userId.toString()),
            new DeliveryOptions().addHeader(MainVerticle.UID, userId.toString()))
        .map(message -> (Boolean) message.body());
  }

  public void publish(UUID gameId, String topic, String action, UUID userId, Object message) {
    eventBus.publish(
        "external.outgoing.game." + gameId,
        new JsonObject()
            .put("gameId", gameId.toString())
            .put("topic", topic)
            .put("action", action)
            .put("userId", userId.toString())
            .put("payload", message));
  }
}
