package it.vitalegi.rpgboard.be;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
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

public class BoardVerticle extends AbstractVerticle {
	Logger log = LoggerFactory.getLogger(BoardVerticle.class);

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		log.info("start");

		EventBus eventBus = vertx.eventBus();

		eventBus.consumer("board.add").handler(msg -> this.addAccount(msg).onSuccess(v -> {
			log.info("Insert record done");
			msg.reply("");
		}).onFailure(e -> handleException("addAccount 1", msg, e)));

		eventBus.consumer("board.getAll").handler(msg -> getAccounts().onSuccess(accounts -> {
			log.info("getAccounts ok");
			msg.reply(Json.encodePrettily(mapAccounts(accounts)));
		}).onFailure(e -> handleException("getAccounts", msg, e)));

		eventBus.consumer("board.sample").handler(this::multipleActions);
	}

	protected Future<Void> addAccount(Message<?> msg) {
		AccountRepository accountRepository = new AccountRepository();

		JsonObject request = JsonObject.mapFrom(msg.body());

		return pool().getConnection()//
				.compose(conn -> conn//
						.preparedQuery(accountRepository.add())//
						.execute(Tuple.of(request.getString("id"), request.getString("name"))) //
						.map((Void) null)//
						.eventually(v -> conn.close())//
				);
	}

	protected Future<List<Account>> getAccounts() {
		AccountRepository accountRepository = new AccountRepository();

		return pool().getConnection()//
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
				);
	}

	protected void multipleActions(Message<?> msg) {
		AccountRepository accountRepository = new AccountRepository();

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

	protected <E> JsonArray mapAccounts(List<E> list) {
		JsonArray jsonArr = new JsonArray();
		list.stream().map(JsonObject::mapFrom).forEach(jsonArr::add);
		return jsonArr;
	}

	protected void handleException(String description, Message<?> msg, Throwable throwable) {
		log.error("Operation failed - {}", description, throwable);
		msg.fail(0, throwable.getMessage());
	}
}
