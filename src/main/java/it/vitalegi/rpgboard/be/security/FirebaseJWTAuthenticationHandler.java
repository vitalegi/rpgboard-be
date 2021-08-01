package it.vitalegi.rpgboard.be.security;

import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.AuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirebaseJWTAuthenticationHandler implements AuthenticationHandler {
  static Logger log = LoggerFactory.getLogger(FirebaseJWTAuthenticationHandler.class);

  protected FirebaseJWTAuthProvider provider;

  @Override
  public io.vertx.ext.web.handler.AuthenticationHandler getDelegate() {
    return null;
  }

  @Override
  public void handle(RoutingContext ctx) {
    try {
      String token = getToken(ctx);
      ctx.setUser(provider.getUser(token));
      ctx.next();
    } catch (InvalidTokenException e) {
      ctx.fail(401, new IllegalStateException("Unauthorized request"));
    } catch (Throwable e) {
      ctx.fail(500, new IllegalStateException("Internal Server Error"));
    }
  }

  @Override
  public String authenticateHeader(RoutingContext ctx) {
    return null;
  }

  @Override
  public void postAuthentication(RoutingContext ctx) {}

  protected String getToken(RoutingContext ctx) {
    return ctx.request().getHeader("Authorization");
  }

  public void setProvider(FirebaseJWTAuthProvider provider) {
    this.provider = provider;
  }
}
