package it.vitalegi.rpgboard.be.security;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.DeliveryContext;
import io.vertx.reactivex.ext.auth.User;
import it.vitalegi.rpgboard.be.MainVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketAuthValidator implements Handler<DeliveryContext<Object>> {

  Logger log = LoggerFactory.getLogger(WebSocketAuthValidator.class);
  private final Vertx vertx;
  private final AuthProvider authProvider;

  public WebSocketAuthValidator(Vertx vertx, AuthProvider authProvider) {
    this.vertx = vertx;
    this.authProvider = authProvider;
  }

  @Override
  public void handle(DeliveryContext<Object> dc) {
    if (!dc.message().address().startsWith("external.incoming")) {
      dc.next();
      return;
    }
    log.info("> {}", dc.message().headers());
    dc.message().headers().set(MainVerticle.UID, "");

    if (dc.message().body() instanceof JsonObject) {
      JsonObject body = (JsonObject) dc.message().body();
      String token = body.getString("authorization", "");
      vertx.executeBlocking(
          future -> {
            User user = authProvider.getUser(token);
            dc.message()
                .headers()
                .set(MainVerticle.UID, user.principal().getString(MainVerticle.UID));
            dc.next();
            future.complete();
          });
    } else {
      log.error("Message received is not a JsonObject");
      dc.next();
    }
  }
}
