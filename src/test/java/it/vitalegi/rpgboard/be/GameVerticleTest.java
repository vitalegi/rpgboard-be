package it.vitalegi.rpgboard.be;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import it.vitalegi.rpgboard.be.data.Game;
import it.vitalegi.rpgboard.be.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

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
  void addGameShouldCallServiceAndComplete(Vertx vertx, VertxTestContext testContext) {
    GameService service = mock(GameService.class);
    gameVerticle.gameService = service;
    when(service.addGame(any(), any())).thenReturn(Single.just(new Game()));

    vertx
        .eventBus()
        .request(
            "game.add", new JsonObject().put("name", "A").put("ownerId", "B").put("open", true))
        .onSuccess(
            msg -> {
              testContext.verify(
                  () -> {
                    verify(service, times(1)).addGame(any(), any());
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  void addGameMissingFieldShouldFail(Vertx vertx, VertxTestContext testContext) throws Throwable {
    GameService service = mock(GameService.class);
    gameVerticle.gameService = service;
    when(service.addGame(any(), any())).thenReturn(Single.just(new Game()));

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
}
