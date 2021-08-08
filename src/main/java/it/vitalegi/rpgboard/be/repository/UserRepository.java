package it.vitalegi.rpgboard.be.repository;

import it.vitalegi.rpgboard.be.data.User;
import it.vitalegi.rpgboard.be.reactivex.data.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class UserRepository extends AbstractSinglePkCrudRepository<User, String> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  public UserRepository() {
    super(Mappers.USER, User::map, User::mapPK, User.BUILDER);
  }
}
