package it.vitalegi.rpgboard.be.service;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.User;
import it.vitalegi.rpgboard.be.repository.UserRepository;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.UUID;

@Singleton
public class UserServiceLocal {
  @Inject protected UserRepository userRepository;

  Logger log = LoggerFactory.getLogger(UserServiceLocal.class);

  public Single<User> register(SqlConnection conn, String externalUserId, String name) {
    notNull(externalUserId, "externalUserId null");
    notNull(name, "name null");
    User user = new User();
    user.setExternalUserId(externalUserId);
    user.setName(name);
    OffsetDateTime now = OffsetDateTime.now();
    user.setCreateDate(now);
    user.setLastUpdate(now);
    return Single.just(user)
        .flatMap(u -> userRepository.add(conn, user).singleOrError())
        .map(VertxUtil.logEntry("user created", User::toString));
  }

  public Maybe<User> findByExternalUserId(SqlConnection conn, String externalUserId) {
    notNull(externalUserId, "externalUserId null");
    return Maybe.just(externalUserId)
        .flatMap(id -> userRepository.findByExternalUserId(conn, externalUserId))
        .map(VertxUtil.debug("user found", User::toString));
  }

  public Single<User> getUser(SqlConnection conn, UUID userId) {
    return userRepository.getById(conn, userId).singleOrError();
  }

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }
}
