package it.vitalegi.rpgboard.be;

import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketConfig {
  static Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

  public static void init(Vertx vertx, Router router) {
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    sockJSHandler.bridge(
        new SockJSBridgeOptions()
            .addInboundPermitted(
                new PermittedOptions().setAddressRegex("external.incoming.*")
                //        .setRequiredAuthority("REGISTERED_USER")
                )
            .addOutboundPermitted(new PermittedOptions().setAddressRegex("external.outgoing.*")));

    /*router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        FirebaseJWTAuthenticationHandler authHandler =
            new FirebaseJWTAuthenticationHandler() {
              @Override
              protected String getToken(RoutingContext ctx) {
                return ctx.request().params().get("jwt");
              }
            };
        router.route("/eventbus/*").blockingHandler(authHandler, false);
    */
    router.route("/eventbus/*").handler(sockJSHandler);
    // router.mountSubRouter("/eventbus", sockJSBridge);
  }
}
