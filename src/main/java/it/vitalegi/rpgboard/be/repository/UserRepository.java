package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Maybe;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.User;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class UserRepository extends AbstractSinglePkCrudRepository<User, UUID> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public UserRepository() {
    super(Mappers.USER, User::map, User::mapPK, User.BUILDER);
  }

  public Maybe<User> findByExternalUserId(SqlConnection conn, String externalUserId) {
    return getByField(conn, User.EXTERNAL_USER_ID, externalUserId)
        .toMaybe()
        .filter(users -> !users.isEmpty())
        .map(users -> users.get(0));
  }
}
