package it.vitalegi.rpgboard.be.service;

import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.User;
import it.vitalegi.rpgboard.be.repository.UserRepository;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserServiceLocal {
  @Inject protected UserRepository userRepository;

  Logger log = LoggerFactory.getLogger(UserServiceLocal.class);

  public Single<User> register(SqlConnection conn, String userId, String name) {
    notNull(userId, "userId null");
    notNull(name, "name null");
    User user = new User();
    user.setId(userId);
    user.setName(name);
    return Single.just(user)
        .flatMap(u -> userRepository.add(conn, user).singleOrError())
        .map(VertxUtil.logEntry("user created", User::toString));
  }

  protected void notNull(Object obj, String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }
}
