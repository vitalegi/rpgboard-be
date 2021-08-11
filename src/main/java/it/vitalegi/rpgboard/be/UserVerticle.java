package it.vitalegi.rpgboard.be;

import io.micronaut.context.BeanContext;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.SqlConnection;
import it.vitalegi.rpgboard.be.service.UserServiceLocal;
import it.vitalegi.rpgboard.be.util.JsonObserver;
import it.vitalegi.rpgboard.be.util.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

public class UserVerticle extends AbstractVerticle {
  static Logger log = LoggerFactory.getLogger(UserVerticle.class);
  protected UserServiceLocal userServiceLocal;
  protected PgPool client;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    log.info("Start");
    EventBus eventBus = vertx.eventBus();
    vertx.exceptionHandler(
        e -> log.error("Unhandled exception {}: {}", e.getClass().getName(), e.getMessage(), e));

    BeanContext beanContext = BeanContext.run();
    beanContext.registerSingleton(config());
    beanContext.registerSingleton(vertx);

    userServiceLocal = beanContext.getBean(UserServiceLocal.class);
    client = beanContext.getBean(PgPool.class);

    eventBus.consumer("user.registration", this::registerUser);
    eventBus.consumer("user.findByExternalUserId", this::findByExternalUserId);
    log.info("Start done");
    startPromise.complete();
  }

  protected void registerUser(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "registerUser");
    JsonObject body = msg.body();
    tx(conn -> {
          String name = body.getString("name");
          return Single.just(msg)
              .flatMap(m -> userServiceLocal.register(conn, getExternalUserId(msg), name))
              .toMaybe();
        })
        .subscribe(observer);
  }

  protected void findByExternalUserId(Message<JsonObject> msg) {
    JsonObserver observer = JsonObserver.init(msg, "findByExternalUserId");
    JsonObject body = msg.body();
    tx(conn -> {
          log.info("Start processing {}", body.encodePrettily());
          String externalUserId = body.getString("externalUserId");
          return Single.just(msg)
              .flatMapMaybe(m -> userServiceLocal.findByExternalUserId(conn, externalUserId))
              .onErrorReturn(
                  e -> {
                    log.error("error on user verticle", e);
                    return null;
                  })
              .map(
                  u -> {
                    log.info("Utente recuperato nel verticle");
                    return u;
                  });
        })
        .subscribe(observer);
  }

  protected <E> Function<E, E> notNull(Function<E, Object> extractor, String field) {
    return obj -> {
      if (extractor.apply(obj) == null) {
        throw new IllegalArgumentException("Argument " + field + " must be not null");
      }
      return obj;
    };
  }

  protected <T> Maybe<T> tx(Function<SqlConnection, Maybe<T>> function) {
    return client.rxWithTransaction(function);
  }

  @Singleton
  protected PgPool getClient(Vertx vertx, JsonObject config) {
    return VertxUtil.pool(vertx, config);
  }

  protected UUID getUserId(Message<JsonObject> msg) {
    return UUID.fromString(msg.headers().get(MainVerticle.UID));
  }

  protected String getExternalUserId(Message<JsonObject> msg) {
    return msg.headers().get(MainVerticle.EXTERNAL_UID);
  }
}
