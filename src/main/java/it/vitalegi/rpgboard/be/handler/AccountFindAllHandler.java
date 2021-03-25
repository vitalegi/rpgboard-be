package it.vitalegi.rpgboard.be.handler;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.RoutingContext;

public class AccountFindAllHandler extends AbstractHandler {

  public AccountFindAllHandler(EventBus eventBus) {
    super(eventBus);
  }

  protected void doHandle(RoutingContext event) {
    eventBus.request("account.getAll", "", reply -> handleResponse(event, reply));
  }
}
