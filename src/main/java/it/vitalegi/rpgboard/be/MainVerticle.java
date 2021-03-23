package it.vitalegi.rpgboard.be;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ErrorHandler;

public class MainVerticle extends AbstractVerticle {
	Logger log = LoggerFactory.getLogger(MainVerticle.class);

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		log.info("start");
		vertx.deployVerticle(new BoardVerticle());
		vertx.deployVerticle(new AccountVerticle());

		Router router = Router.router(vertx);

		EventBus eventBus = vertx.eventBus();

		router.get("/api/board").handler(context -> {
			JsonObject message = new JsonObject();
			eventBus.request("board.getAll", message, reply -> handleResponse(context, reply));
		});

		router.post("/api/board").handler(context -> {
			JsonObject message = new JsonObject();
			message.put("id", context.queryParam("id").get(0));
			message.put("name", context.queryParam("name").get(0));
			eventBus.request("board.add", message, reply -> handleResponse(context, reply));
		});

		router.get("/api/account").handler(context -> {
			JsonObject message = new JsonObject();
			eventBus.request("account.getAll", message, reply -> handleResponse(context, reply));
		});
		router.post("/api/account").handler(context -> {
			JsonObject message = new JsonObject();
			message.put("id", context.queryParam("id").get(0));
			message.put("name", context.queryParam("name").get(0));
			eventBus.request("account.add2", message, reply -> handleResponse(context, reply));
		});

		router.get("/api/test").handler(context -> {
			String address = context.request().connection().remoteAddress().toString();
			MultiMap queryParams = context.queryParams();
			String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";

			context.json(new JsonObject()//
					.put("name", name)//
					.put("address", address)//
					.put("message", "Hello " + name + " connected from " + address));
		});

		Handler<ServerWebSocket> wsHandler = (context) -> {
			context.writeTextMessage("ping");
			context.textMessageHandler((msg) -> {
				System.out.println("Server " + msg);

				if ((new Random()).nextInt(100) == 0) {
					context.close();
				} else {
					context.writeTextMessage("ping2");
				}
			});
		};
		vertx.createHttpServer().requestHandler(router)//
				.webSocketHandler(wsHandler)//
				.listen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")//
				.onSuccess(server -> {
					System.out.println("Start server on port " + server.actualPort());
					startPromise.complete();
				}).onFailure(cause -> {
					System.err.println(cause);
					startPromise.fail(cause);

				});
	}

	private <T> void handleResponse(RoutingContext context, AsyncResult<Message<T>> reply) {
		if (reply.succeeded()) {
			HttpServerResponse response = context.response();
			response.putHeader("content-type", "application/json; charset=utf-8");
			response.end(reply.result().body().toString());
		} else {
			context.response().setStatusCode(500);
			context.json(new JsonObject().put("error", //
					reply.cause().getClass().getName()) //
					.put("description", reply.cause().getMessage()));
		}
	}
}
