package it.vitalegi.rpgboard.be.mapper;

import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.vertx.core.json.JsonObject;
import it.vitalegi.rpgboard.be.data.GamePlayerRole;
import it.vitalegi.rpgboard.be.data.User;
import it.vitalegi.rpgboard.be.util.VertxUtil;

import java.util.List;
import java.util.stream.Collectors;

public class GameMapper {
  public static Single<JsonObject> mapGamePlayer(
      Single<User> user, Single<List<GamePlayerRole>> roles) {
    return Single.zip(user, roles, GameMapper::mapGamePlayer);
  }

  @NonNull
  public static JsonObject mapGamePlayer(User user, List<GamePlayerRole> roles) {
    return new JsonObject()
        .put("action", "JOIN")
        .put("user", JsonObject.mapFrom(user))
        .put(
            "roles",
            VertxUtil.jsonMap(
                roles.stream().map(GamePlayerRole::getRole).collect(Collectors.toList())));
  }
}
