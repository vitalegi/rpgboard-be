package it.vitalegi.rpgboard.be.handler;

import java.util.Arrays;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import it.vitalegi.rpgboard.be.User;

public class UserHandler extends AbstractHandler {

	public void add(RoutingContext context) {
		jsonResponse(context).end(Json.encodePrettily(new User("aa", "Bb")));
	}

	public void getAll(RoutingContext context) {
		jsonResponse(context).end(Json.encodePrettily(Arrays.asList(new User("aa", "Bb"), new User("ccc", "ddd"))));
	}
}
