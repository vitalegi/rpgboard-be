package it.vitalegi.rpgboard.be.repository;

import io.reactivex.Observable;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.data.User;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class UserRepository extends AbstractSinglePkCrudRepository<User, String> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public UserRepository() {
    super(Mappers.USER, User::map, User::mapPK, User.BUILDER);
  }

  public Observable<User> add(SqlConnection connection, User entry) {
    return updateSingle(
            connection,
            builder.insert().values().all().build(),
            entryMapper.apply(entry));
  }

}
