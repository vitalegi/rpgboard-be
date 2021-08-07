package it.vitalegi.rpgboard.be.security;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.reactivex.ext.web.handler.sockjs.BridgeEvent;
import it.vitalegi.rpgboard.be.MainVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class WebSocketBridgeListener implements Handler<BridgeEvent> {
  Logger log = LoggerFactory.getLogger(this.getClass());
  AuthProvider authProvider;

  public WebSocketBridgeListener(AuthProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public void handle(BridgeEvent event) {
    String id = event.socket().webSession().id();
    if (Arrays.asList(BridgeEventType.SOCKET_CREATED, BridgeEventType.UNREGISTER)
        .contains(event.type())) {
      log.info("WEB_SOCKET type={}, id={}, address={}", event.type(), id, getAddress(event));
      event.complete(true);
      return;
    }
    if (Arrays.asList(BridgeEventType.PUBLISH, BridgeEventType.SEND, BridgeEventType.REGISTER)
        .contains(event.type())) {
      String userId = getUserId(event.getRawMessage());
      boolean accept = userId != null;
      log.info(
          "WEB_SOCKET type={}, id={}, address={}, accepted={}, userId={}",
          event.type(),
          id,
          getAddress(event),
          accept,
          userId);
      if (userId != null) {
        event.getRawMessage().getJsonObject("headers").put(MainVerticle.UID, userId);
      }
      event.complete(accept);
      return;
    }
    log.debug("WEB_SOCKET type={}, id={}, address={}", event.type(), id, getAddress(event));
    event.complete(true);
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

  protected String getAddress(BridgeEvent event) {
    if (event != null && event.getRawMessage() != null) {
      return event.getRawMessage().getString("address");
    }
    return null;
  }
}
