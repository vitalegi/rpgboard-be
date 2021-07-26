package it.vitalegi.rpgboard.be;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import io.reactivex.Completable;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import it.vitalegi.rpgboard.be.handler.AccountAddHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindAllHandler;
import it.vitalegi.rpgboard.be.handler.AccountFindByIdHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainVerticle extends AbstractVerticle {
  Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Completable rxStart() {
    log.info("start");

    vertx.deployVerticle(new AccountVerticle());
    vertx.deployVerticle(new GameVerticle());
    Router router = Router.router(vertx);

    initFirebase();
    router
        .route()
        .handler(
            CorsHandler.create("http://localhost:8080")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("Content-Type")
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("x-requested-with")
                .allowedHeader("origin")
                .allowedHeader("accept")
                .allowCredentials(true));

    EventBus eventBus = vertx.eventBus();

    router.get("/api/account").handler(new AccountFindByIdHandler(eventBus));
    router.post("/api/account").handler(new AccountAddHandler(eventBus));
    router.get("/api/accounts").handler(new AccountFindAllHandler(eventBus));

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    sockJSHandler.bridge(
        new SockJSBridgeOptions()
            .addInboundPermitted(new PermittedOptions().setAddressRegex("external.*"))
            .addOutboundPermitted(new PermittedOptions().setAddressRegex("external.*")));

    router.route("/eventbus/*").handler(sockJSHandler);

    return vertx
        .createHttpServer()
        .requestHandler(router)
        .rxListen(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")
        .ignoreElement();
  }

  private void initFirebase() {
    log.info("Firebase init");
    String firebasePrivateKey = System.getenv("FIREBASE_PRIVATE_KEY");
    InputStream firebase =
        new ByteArrayInputStream(firebasePrivateKey.getBytes(StandardCharsets.UTF_8));

    FirebaseOptions options = null;
    try {
      options =
          new FirebaseOptions.Builder()
              .setCredentials(GoogleCredentials.fromStream(firebase))
              .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    FirebaseApp.initializeApp(options);
    log.info("Firebase init complete");
  }
}
