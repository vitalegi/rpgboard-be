package it.vitalegi.rpgboard.be.security;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.DeliveryContext;
import io.vertx.reactivex.ext.auth.User;
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
    UserContext.setUserId(dc.message(), "");

    if (dc.message().body() instanceof JsonObject) {
      JsonObject body = (JsonObject) dc.message().body();
      String token = body.getString("authorization", "");
      vertx.executeBlocking(
          future -> {
            User user = authProvider.getUser(token);
            UserContext.setUserId(dc.message(), user.principal().getString("id"));
            dc.next();
            future.complete();
          });
    } else {
      log.error("Message received is not a JsonObject");
      dc.next();
    }
  }
}
