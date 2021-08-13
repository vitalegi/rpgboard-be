package it.vitalegi.rpgboard.be.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class TopicUtil {
  protected static final String GAME_BASE_TOPIC = "external.outgoing.game.";
  protected static final String GAME_PLAYER_TOPIC_REGEX =
      "external\\.outgoing\\.game\\.(.*)\\.players";

  Logger log = LoggerFactory.getLogger(this.getClass());

  public boolean isGameRelatedTopic(String address) {
    return getGameId(address) != null;
  }

  public UUID getGameId(String address) {
    if (address == null) {
      return null;
    }
    if (address.startsWith(GAME_BASE_TOPIC)) {
      String suffix = address.substring(GAME_BASE_TOPIC.length());
      if (suffix.contains(".")) {
        return UuidUtil.getUUID(suffix.substring(0, suffix.indexOf(".")));
      }
      return UuidUtil.getUUID(suffix);
    }
    return null;
  }

  public boolean isGamePlayerTopic(String address) {
    if (address == null) {
      return false;
    }
    return address.matches(GAME_PLAYER_TOPIC_REGEX);
  }
}
