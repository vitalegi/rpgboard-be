package it.vitalegi.rpgboard.be.handler;

import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

public class AccountFindByIdHandler extends AbstractHandler {

  public AccountFindByIdHandler(EventBus eventBus) {
    super(eventBus);
  }

  protected void doHandle(RoutingContext event) {
    JsonObject message = new JsonObject();
    message.put("id", event.queryParam("id").get(0));
    eventBus.request("account.get", message, reply -> handleResponse(event, reply));
  }
}
