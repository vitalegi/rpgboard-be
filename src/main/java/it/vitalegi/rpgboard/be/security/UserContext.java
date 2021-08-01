package it.vitalegi.rpgboard.be.security;

import io.vertx.reactivex.core.eventbus.Message;

public class UserContext {

  private static final String USER_ID = "userId";

  public static String getUserId(Message<?> message) {
    return message.headers().get(USER_ID);
  }

  public static void setUserId(Message<?> message, String userId) {
    message.headers().add(USER_ID, userId);
  }

  public static boolean isAuthenticated(Message<?> message) {
    String id = getUserId(message);
    return id != null && id.trim().length() > 0;
  }
}
