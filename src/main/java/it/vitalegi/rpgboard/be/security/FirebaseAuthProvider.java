package it.vitalegi.rpgboard.be.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.authorization.Authorization;
import io.vertx.reactivex.ext.auth.authorization.PermissionBasedAuthorization;
import it.vitalegi.rpgboard.be.MainVerticle;
import it.vitalegi.rpgboard.be.exception.InvalidTokenException;
import it.vitalegi.rpgboard.be.logging.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class FirebaseAuthProvider extends AuthProvider {
  public static final String METHOD_NAME = "FIREBASE";
  static Logger log = LoggerFactory.getLogger(FirebaseAuthProvider.class);

  public static void init(JsonObject config) {
    log.info("Init");

    if (!config.getJsonObject("security").getString("auth", "").equals(METHOD_NAME)) {
      log.info("Auth method is NOT firebase, skip init");
      return;
    }
    String firebasePrivateKey = config.getString("FIREBASE_PRIVATE_KEY");
    InputStream firebase =
        new ByteArrayInputStream(firebasePrivateKey.getBytes(StandardCharsets.UTF_8));

    FirebaseOptions options = null;
    try {
      options =
          new FirebaseOptions.Builder()
              .setCredentials(GoogleCredentials.fromStream(firebase))
              .build();
    } catch (IOException e) {
      throw new InvalidTokenException(e);
    }

    FirebaseApp.initializeApp(options);
    log.info("Init completed");
  }

  public Single<User> getUser(String token) {
    long start = System.currentTimeMillis();
    try {
      FirebaseAuth instance = FirebaseAuth.getInstance();
      FirebaseToken auth = instance.verifyIdToken(token);
      User user = User.fromName(auth.getEmail());
      user.principal().put(MainVerticle.EXTERNAL_UID, auth.getUid());
      user.principal().put("mail", auth.getEmail());
      user.authorizations().add("firebaseJwt", getAuthorizations(auth));
      return fillUser(user, auth.getUid());
    } catch (FirebaseAuthException e) {
      LogUtil.failure("firebase.validate", start, "unknown", "", e);
      throw new InvalidTokenException(e);
    } catch (Throwable e) {
      LogUtil.failure("firebase.validate", start, "unknown", "", e);
      throw e;
    }
  }

  private Set<Authorization> getAuthorizations(FirebaseToken auth) {
    final Set<Authorization> authorizations = new HashSet<>();
    authorizations.add(PermissionBasedAuthorization.create("REGISTERED_USER"));
    return authorizations;
  }
}
