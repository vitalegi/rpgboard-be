package it.vitalegi.rpgboard.be;

import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.pgclient.PgPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(AuthenticationVerticle.class);

  private PgPool client;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("start");
    EventBus eventBus = vertx.eventBus();

    client = VertxUtil.pool(vertx);
  }
}
