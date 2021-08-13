package it.vitalegi.rpgboard.be.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class TopicUtil {
  protected static final String GAME_TOPIC_REGEX = "external\\.outgoing\\.game\\.(.*)";

  Logger log = LoggerFactory.getLogger(this.getClass());

  public UUID getGameId(String address) {
    if (address == null) {
      return null;
    }
    Pattern pattern = Pattern.compile(GAME_TOPIC_REGEX);
    Matcher matcher = pattern.matcher(address);

    if (matcher.find()) {
      return UuidUtil.getUUID(matcher.group(1));
    }
    return null;
  }
}
