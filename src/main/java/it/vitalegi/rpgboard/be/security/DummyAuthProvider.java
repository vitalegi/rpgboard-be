package it.vitalegi.rpgboard.be.security;

import io.reactivex.Single;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.authorization.Authorization;
import io.vertx.reactivex.ext.auth.authorization.PermissionBasedAuthorization;
import it.vitalegi.rpgboard.be.MainVerticle;
import it.vitalegi.rpgboard.be.exception.InvalidTokenException;
import it.vitalegi.rpgboard.be.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class DummyAuthProvider extends AuthProvider {
  public static final String METHOD_NAME = "DUMMY";
  static Logger log = LoggerFactory.getLogger(DummyAuthProvider.class);

  public Single<User> getUser(String uid) {
    if (StringUtil.isNullOrEmpty(uid)) {
      throw new InvalidTokenException("Invalid dummy login");
    }
    User user = User.fromName(uid);
    user.principal().put(MainVerticle.EXTERNAL_UID, uid);

    user.authorizations().add("dummy", getAuthorizations());

    return fillUser(user, uid);
  }

  protected Set<Authorization> getAuthorizations() {
    final Set<Authorization> authorizations = new HashSet<>();
    authorizations.add(PermissionBasedAuthorization.create("REGISTERED_USER"));
    return authorizations;
  }
}
