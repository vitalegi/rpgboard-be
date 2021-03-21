package it.vitalegi.rpgboard.be.handler;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class AbstractHandler {

	protected RoutingContext jsonResponse(RoutingContext context) {
		HttpServerResponse response = context.response();
		response.putHeader("content-type", "application/json; charset=utf-8");
		return context;
	}

}
