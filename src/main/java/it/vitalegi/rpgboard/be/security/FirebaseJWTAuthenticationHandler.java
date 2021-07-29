package it.vitalegi.rpgboard.be.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.authorization.Authorization;
import io.vertx.reactivex.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.AuthenticationHandler;
import it.vitalegi.rpgboard.be.logging.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class FirebaseJWTAuthenticationHandler implements AuthenticationHandler {
  static Logger log = LoggerFactory.getLogger(FirebaseJWTAuthenticationHandler.class);

  @Override
  public io.vertx.ext.web.handler.AuthenticationHandler getDelegate() {
    return null;
  }

  @Override
  public void handle(RoutingContext ctx) {
    HttpServerRequest request = ctx.request();
    HttpServerResponse response = ctx.response();
    try {
      String token = getToken(ctx);
      ctx.setUser(getUser(token));
      ctx.next();
    } catch(InvalidTokenException e) {
      ctx.fail(401, new IllegalStateException("Unauthorized request"));
    }
    catch (Throwable e) {
      ctx.fail(500, new IllegalStateException("Internal Server Error"));
    }
  }

  @Override
  public String authenticateHeader(RoutingContext ctx) {
    return null;
  }

  @Override
  public void postAuthentication(RoutingContext ctx) {}

  public static void init() {
    log.info("Init");
    String firebasePrivateKey = System.getenv("FIREBASE_PRIVATE_KEY");
    InputStream firebase =
        new ByteArrayInputStream(firebasePrivateKey.getBytes(StandardCharsets.UTF_8));

    FirebaseOptions options = null;
    try {
      options =
          new FirebaseOptions.Builder()
              .setCredentials(GoogleCredentials.fromStream(firebase))
              .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    FirebaseApp.initializeApp(options);
    log.info("Init completed");
  }

  protected User getUser(String token) {
    long start = System.currentTimeMillis();
    try {
      FirebaseAuth instance = FirebaseAuth.getInstance();
      FirebaseToken auth = instance.verifyIdToken(token);
      User user = User.fromName(auth.getEmail());
      user.principal().put("id", auth.getUid());
      user.principal().put("mail", auth.getEmail());
      user.authorizations().add("firebaseJwt", getAuthorizations(auth));
      LogUtil.success("firebase.validate", start, auth.getUid(), auth.getEmail());
      return user;
    } catch (FirebaseAuthException e) {
      LogUtil.failure("firebase.validate", start, "unknown", "", e);
      throw new InvalidTokenException(e);
    } catch (Throwable e) {
      LogUtil.failure("firebase.validate", start, "unknown", "", e);
      throw e;
    }
  }

  protected Set<Authorization> getAuthorizations(FirebaseToken auth) {
    final Set<Authorization> authorizations = new HashSet<>();
    authorizations.add(PermissionBasedAuthorization.create("REGISTERED_USER"));
    return authorizations;
  }

  protected String getToken(RoutingContext ctx) {
    return ctx.request().getHeader("Authorization");
  }
}
