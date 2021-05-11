package it.vitalegi.rpgboard.be.handler;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountFindByIdHandler extends AbstractHandler {
  Logger log = LoggerFactory.getLogger(AccountFindByIdHandler.class);

  public AccountFindByIdHandler(EventBus eventBus) {
    super(eventBus);
  }

  protected void doHandle(RoutingContext event) {
    log.info("process findById");
    JsonObject message = new JsonObject();
    message.put("id", event.queryParam("id").get(0));
    eventBus.request("account.get", message, reply -> handleResponse(event, reply));
  }
}
