package it.vitalegi.rpgboard.be.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
  static Logger log = LoggerFactory.getLogger(LogUtil.class);

  public static void success(
      String methodName, long startTime, String uid, String additionalNotes) {
    log.info(
        "APP_STATS method={} status=OK, timeTaken={}, uid={}, {}",
        methodName,
        System.currentTimeMillis() - startTime,
        uid,
        additionalNotes != null ? additionalNotes : "");
  }

  public static void failure(
      String methodName, long startTime, String uid, String additionalNotes, Throwable e) {
    log.info(
        "APP_STATS method={} status=KO, timeTaken={}, uid={}, exception={}, msg={}, root={}, rootMsg={}, {}",
        methodName,
        System.currentTimeMillis() - startTime,
        uid,
        e.getClass().getName(),
        e.getMessage(),
        getRoot(e),
        getRoot(e).getMessage(),
        additionalNotes != null ? additionalNotes : "");
  }

  protected static Throwable getRoot(Throwable e) {
    while (e.getCause() != null) {
      e = e.getCause();
    }
    return e;
  }
}
