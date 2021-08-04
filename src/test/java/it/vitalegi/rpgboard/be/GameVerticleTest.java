package it.vitalegi.rpgboard.be;

import io.reactivex.Observable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.repository.GameRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(VertxExtension.class)
public class GameVerticleTest {
  Logger log = LoggerFactory.getLogger(GameVerticleTest.class);

  GameVerticleMock gameVerticle;

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void prepare(Vertx vertx, VertxTestContext testContext) {
    gameVerticle = new GameVerticleMock();
    vertx.deployVerticle(gameVerticle, testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void addGameRequestMappingShouldBeComplete(Vertx vertx, VertxTestContext testContext) {

    gameVerticle.setGameRepository(
        new GameRepository() {
          @Override
          public Observable<Game> add(Game game) {
            testContext.verify(
                () -> {
                  Assertions.assertEquals("A", game.getName());
                  Assertions.assertEquals("B", game.getOwnerId());
                  Assertions.assertEquals(true, game.getOpen());
                  testContext.completeNow();
                });
            return Observable.just(new Game());
          }
        });
    vertx
        .eventBus()
        .publish(
            "game.add", new JsonObject().put("name", "A").put("ownerId", "B").put("open", true));
  }

  @Test
  void addGameMissingFieldShouldFail(Vertx vertx, VertxTestContext testContext) throws Throwable {
    vertx
        .eventBus()
        .request("game.add", new JsonObject().put("ownerId", "B").put("open", true))
        .onSuccess(game -> testContext.failNow("exception was not thrown"))
        .onFailure(e -> testContext.completeNow());
  }

  @Test
  void gameVerticleIsDeployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }

  private static class GameVerticleMock extends GameVerticle {
    @Override
    protected void initRepositories() {}

    public void setGameRepository(GameRepository gameRepository) {
      this.gameRepository = gameRepository;
    }
  }
}
