package it.vitalegi.rpgboard.be.security;

import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.AuthenticationHandler;
import it.vitalegi.rpgboard.be.MainVerticle;
import it.vitalegi.rpgboard.be.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AuthProvider implements AuthenticationHandler {
  static Logger log = LoggerFactory.getLogger(AuthProvider.class);

  @Override
  public io.vertx.ext.web.handler.AuthenticationHandler getDelegate() {
    return null;
  }

  @Override
  public void handle(RoutingContext ctx) {
    String token = "";
    try {
      token = getToken(ctx);
      User user = getUser(token);
      ctx.setUser(user);
      ctx.put(MainVerticle.UID, user.principal().getString(MainVerticle.UID));
      ctx.next();
    } catch (InvalidTokenException e) {
      log.error("Invalid login, token {}: {}", token, e.getMessage());
      ctx.fail(401, new IllegalStateException("Unauthorized request"));
    } catch (Throwable e) {
      log.error("Generic error, token {}: {}", token, e.getMessage());
      ctx.fail(500, new IllegalStateException("Internal Server Error"));
    }
  }

  @Override
  public String authenticateHeader(RoutingContext context) {
    return null;
  }

  @Override
  public void postAuthentication(RoutingContext ctx) {}

  public abstract User getUser(String token);

  protected String getToken(RoutingContext ctx) {
    String header = ctx.request().getHeader("Authorization");
    if (!StringUtil.isNullOrEmpty(header)) {
      return header;
    }
    return ctx.request().getParam("jwt");
  }
}
