package it.vitalegi.rpgboard.be;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

public class VertxTestUtil {

  public static DeploymentOptions options() {
    return new DeploymentOptions().setConfig(new JsonObject().put("ENV", "test"));
  }
}
