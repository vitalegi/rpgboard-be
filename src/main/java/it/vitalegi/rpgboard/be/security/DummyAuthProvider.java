package it.vitalegi.rpgboard.be.security;

import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;
import it.vitalegi.rpgboard.be.MainVerticle;

public class DummyAuthProvider implements Handler<RoutingContext> {
    public static final String METHOD_NAME="DUMMY";
    @Override
    public void handle(RoutingContext ctx) {
        String uid = ctx.request().getHeader("uid");
        ctx.put(MainVerticle.UID, uid);
        ctx.next();
    }
}
