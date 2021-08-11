package it.vitalegi.rpgboard.be.util;

import java.util.UUID;

public class UuidUtil {

  public static UUID getUUID(String name) {
    if (name == null) {
      return null;
    }
    try {
      return UUID.fromString(name);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("UUID not valid [" + name + "]", e);
    }
  }
}
