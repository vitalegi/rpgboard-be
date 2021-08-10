package it.vitalegi.rpgboard.be.util;

import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {
  public static boolean isNullOrEmpty(String str) {
    return str == null || str.length() == 0;
  }

  public static boolean isNotNullOrEmpty(String str) {
    return !isNullOrEmpty(str);
  }

  public static List<String> diff(List<String> list, List<String> toRemove) {
    return list.stream().filter(el -> !toRemove.contains(el)).collect(Collectors.toList());
  }
}
