package it.vitalegi.rpgboard.be.security;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.DeliveryContext;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.auth.User;
import it.vitalegi.rpgboard.be.MainVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirebaseJWTDeliveryContext implements Handler<DeliveryContext<Object>> {

  Logger log = LoggerFactory.getLogger(FirebaseJWTDeliveryContext.class);
  private Vertx vertx;
  private FirebaseJWTAuthProvider authProvider;

  public FirebaseJWTDeliveryContext(Vertx vertx, FirebaseJWTAuthProvider authProvider) {
    this.vertx = vertx;
    this.authProvider = authProvider;
  }

  @Override
  public void handle(DeliveryContext<Object> dc) {
    if (!dc.message().address().startsWith("external.incoming")) {
      dc.next();
      return;
    }
    setUserId(dc.message(), "");

    if (dc.message().body() instanceof JsonObject) {
      JsonObject body = (JsonObject) dc.message().body();
      String token = body.getString("authorization", "");
      vertx.executeBlocking(
          future -> {
            User user = authProvider.getUser(token);
            setUserId(dc.message(), user.principal().getString(MainVerticle.UID));
            dc.next();
            future.complete();
          });
    } else {
      log.error("Message received is not a JsonObject");
      dc.next();
    }
  }

  protected String getUserId(Message<?> message) {
    return message.headers().get(MainVerticle.UID);
  }

  protected void setUserId(Message<?> message, String userId) {
    message.headers().add(MainVerticle.UID, userId);
  }
}
