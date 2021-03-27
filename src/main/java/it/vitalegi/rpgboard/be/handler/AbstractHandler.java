package it.vitalegi.rpgboard.be.handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

public abstract class AbstractHandler implements Handler<RoutingContext> {

  protected EventBus eventBus;

  public AbstractHandler(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void handle(RoutingContext context) {
    try {
      doHandle(context);
    } catch (Exception e) {
      handleError(context, e);
    }
  }

  protected abstract void doHandle(RoutingContext event);

  protected <T> void handleResponse(RoutingContext context, AsyncResult<Message<T>> reply) {
    if (reply.succeeded()) {
      HttpServerResponse response = context.response();
      response.putHeader("content-type", "application/json; charset=utf-8");
      response.end(reply.result().body().toString());
    } else {
      handleError(context, reply.cause());
    }
  }

  protected void handleError(RoutingContext context, Throwable t) {
    context.response().setStatusCode(500);
    context.json(
        new JsonObject().put("error", t.getClass().getName()).put("description", t.getMessage()));
  }
}
