package it.vitalegi.rpgboard.be.security;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.AuthenticationHandler;
import it.vitalegi.rpgboard.be.MainVerticle;
import it.vitalegi.rpgboard.be.exception.InvalidTokenException;
import it.vitalegi.rpgboard.be.util.StringUtil;
import it.vitalegi.rpgboard.be.util.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;

public abstract class AuthProvider implements AuthenticationHandler {
  static Logger log = LoggerFactory.getLogger(AuthProvider.class);

  @Inject EventBus eventBus;

  @Override
  public io.vertx.ext.web.handler.AuthenticationHandler getDelegate() {
    return null;
  }

  @Override
  public void handle(RoutingContext ctx) {
    String token = getToken(ctx);
    getUser(token)
        .subscribe(
            user -> {
              ctx.setUser(user);
              ctx.put(MainVerticle.UID, user.principal().getString(MainVerticle.UID));
              ctx.put(
                  MainVerticle.EXTERNAL_UID, user.principal().getString(MainVerticle.EXTERNAL_UID));
              ctx.next();
            },
            e -> {
              if (e instanceof InvalidTokenException) {
                log.error("Invalid login, token {}: {}", token, e.getMessage());
                ctx.fail(401, new IllegalStateException("Unauthorized request"));
                return;
              }
              log.error("Generic error, token {}: {}", token, e.getMessage());
              ctx.fail(500, new IllegalStateException("Internal Server Error"));
            });
  }

  @Override
  public String authenticateHeader(RoutingContext context) {
    return null;
  }

  @Override
  public void postAuthentication(RoutingContext ctx) {}

  public abstract Single<User> getUser(String token);

  protected String getToken(RoutingContext ctx) {
    String header = ctx.request().getHeader("Authorization");
    if (!StringUtil.isNullOrEmpty(header)) {
      return header;
    }
    return ctx.request().getParam("jwt");
  }

  protected Maybe<UUID> findByExternalUserId(String externalUserId) {
    log.debug("Search user by {}", externalUserId);
    return eventBus
        .rxRequest(
            "user.findByExternalUserId", new JsonObject().put("externalUserId", externalUserId))
        .toMaybe()
        .filter(msg -> getUUID(msg) != null)
        .map(this::getUUID);
  }

  protected Single<User> fillUser(User user, String externalUserId) {
    return findByExternalUserId(externalUserId)
        .map(UUID::toString)
        .switchIfEmpty(Single.just(""))
        .map(
            uuid -> {
              log.info("Recuperato {}", uuid);
              user.principal().put(MainVerticle.UID, uuid);
              return user;
            });
  }

  protected UUID getUUID(Message<Object> msg) {
    JsonObject body = (JsonObject) msg.body();
    return UuidUtil.getUUID(body.getString("userId"));
  }
}
