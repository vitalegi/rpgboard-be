package it.vitalegi.rpgboard.be.repository;

import io.vertx.reactivex.sqlclient.templates.RowMapper;

public class DefaultCrudRepository<E> extends DatabaseProxy<E> {

  protected RowMapper<E> rowMapper;

  public DefaultCrudRepository(RowMapper<E> rowMapper) {
    this.rowMapper = rowMapper;
  }

  protected RowMapper<E> rowMapper() {
    return rowMapper;
  }
}
