package it.vitalegi.rpgboard.be;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import it.vitalegi.rpgboard.be.repository.AccountRepository;

public class RpgBoardVerticle extends AbstractVerticle {
	Logger log = LoggerFactory.getLogger(RpgBoardVerticle.class);

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		log.info("start");

		EventBus eventBus = vertx.eventBus();
		AccountRepository accountRepository = new AccountRepository();

		eventBus.consumer("rpgboard.add").handler(this::addAccount);
		eventBus.consumer("rpgboard.getAll").handler(this::getAccounts);

		eventBus.consumer("rpgboard.add2").handler(msg -> {

			pool().query(accountRepository.getAll()).execute(ar -> {
				if (ar.succeeded()) {
					RowSet<Row> result = ar.result();
					System.out.println("Got " + result.size() + " rows ");
				} else {
					System.out.println("Failure: " + ar.cause().getMessage());
				}
			});

			pool().getConnection()//
					.compose(conn -> conn//
							.preparedQuery(accountRepository.add())//
							.execute(Tuple.of("A", "B")) //
							.compose(rowSet -> conn//
									.query(accountRepository.getAll())//
									.execute()//
									.map(rows -> rows.iterator().next().getInteger(0)))
							.eventually(v -> conn.close())//
			).onSuccess(count -> {
				log.info("Query executed successfully");
				msg.reply(Json.encodePrettily(new Account("aa", "" + count)));
			}).onFailure(ex -> {
				log.error("Failed to process query", ex);
				msg.fail(0, ex.getMessage());
			});
		});
	}

	protected void addAccount(Message<?> msg) {
		AccountRepository accountRepository = new AccountRepository();

		JsonObject request = JsonObject.mapFrom(msg.body());

		pool().getConnection()//
				.compose(conn -> conn//
						.preparedQuery(accountRepository.add())//
						.execute(Tuple.of(request.getString("id"), request.getString("name"))) //
						.eventually(v -> conn.close())//
				).onSuccess(v -> {
					log.info("Insert record done");
					msg.reply("");
				}).onFailure(ex -> {
					log.error("Insert record failed", ex);
					msg.fail(0, ex.getMessage());
				});
	}

	protected void getAccounts(Message<?> msg) {
		AccountRepository accountRepository = new AccountRepository();

		pool().getConnection()//
				.compose(conn -> conn//
						.query(accountRepository.getAll())//
						.execute()//
						.map(rows -> {
							Iterable<Row> iterable = () -> rows.iterator();
							return StreamSupport.stream(iterable.spliterator(), false) //
									.map(this::mapAccount) //
									.collect(Collectors.toList());
						}) //
						.eventually(v -> conn.close())//
				).onSuccess(accounts -> {
					log.info("Query executed successfully");
					JsonArray arr = new JsonArray();
					accounts.stream().map(JsonObject::mapFrom).forEach(arr::add);
					msg.reply(Json.encodePrettily(arr));
				}).onFailure(ex -> {
					log.error("Failed to process query", ex);
					msg.fail(0, ex.getMessage());
				});
	}

	protected PgPool pool() {
		PgConnectOptions connectOptions = PgConnectOptions.fromUri(System.getenv("JDBC_DATABASE_URL"));

		// Pool options
		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		// Create the pooled client
		return PgPool.pool(vertx, connectOptions, poolOptions);
	}

	protected Account mapAccount(Row row) {
		Account account = new Account();
		account.setId(row.getString(0));
		account.setName(row.getString(1));
		return account;
	}
}
