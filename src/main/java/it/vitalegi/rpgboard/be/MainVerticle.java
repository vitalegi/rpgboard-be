package it.vitalegi.rpgboard.be;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) throws Exception {

		Router router = Router.router(vertx);

		router.route().handler(context -> {
			String address = context.request().connection().remoteAddress().toString();
			MultiMap queryParams = context.queryParams();
			String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";

			context.json(new JsonObject()//
					.put("name", name)//
					.put("address", address)//
					.put("message", "Hello " + name + " connected from " + address));
		});

		vertx.createHttpServer().requestHandler(router)//
				.listen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")//
				.onSuccess(server -> {
					System.out.println("Start server on port " + server.actualPort());
					startPromise.complete();
				}).onFailure(cause -> {
					System.err.println(cause);
					startPromise.fail(cause);

				});
	}
}
