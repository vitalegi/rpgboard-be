package it.vitalegi.rpgboard.be.handler;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.RoutingContext;

public class AccountAddHandler extends AbstractHandler {

  public AccountAddHandler(EventBus eventBus) {
    super(eventBus);
  }

  protected void doHandle(RoutingContext event) {
    JsonObject message = new JsonObject();
    message.put("id", event.queryParam("id").get(0));
    message.put("name", event.queryParam("name").get(0));
    eventBus.request("account.add", message, reply -> handleResponse(event, reply));
  }
}
