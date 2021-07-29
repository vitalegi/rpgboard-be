package it.vitalegi.rpgboard.be;

import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
import it.vitalegi.rpgboard.be.security.FirebaseJWTAuthenticationHandler;

public class WebSocketConfig {

  public static void init(Vertx vertx, Router router) {
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    Router sockJSBridge =
        sockJSHandler.bridge(
            new SockJSBridgeOptions()
                .addInboundPermitted(
                    new PermittedOptions()
                        .setAddressRegex("external.*")
                        .setRequiredAuthority("REGISTERED_USER"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("external.*")));

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    FirebaseJWTAuthenticationHandler authHandler =
        new FirebaseJWTAuthenticationHandler() {
          @Override
          protected String getToken(RoutingContext ctx) {
            return ctx.request().params().get("jwt");
          }
        };
    router.route("/eventbus/*").blockingHandler(authHandler, false);

    router.mountSubRouter("/eventbus", sockJSBridge);
  }
}
