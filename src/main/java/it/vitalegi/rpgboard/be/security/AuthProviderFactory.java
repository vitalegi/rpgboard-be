package it.vitalegi.rpgboard.be.security;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthProviderFactory {
  Logger log = LoggerFactory.getLogger(this.getClass());
  JsonObject config;

  public AuthProviderFactory(JsonObject config) {
    this.config = config;
  }

  public AuthProvider getProvider() {
    String authMethod = config.getJsonObject("security").getString("auth");
    if (authMethod.equals(DummyAuthProvider.METHOD_NAME)) {
      log.info("DUMMY auth method");
      return new DummyAuthProvider();
    }
    if (authMethod.equals(FirebaseAuthProvider.METHOD_NAME)) {
      log.info("FIREBASE auth method");
      return new FirebaseAuthProvider();
    }
    throw new IllegalArgumentException("Invalid auth method " + authMethod);
  }
}
