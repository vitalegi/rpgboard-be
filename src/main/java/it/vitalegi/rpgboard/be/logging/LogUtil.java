package it.vitalegi.rpgboard.be.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class LogUtil {
  static Logger log = LoggerFactory.getLogger(LogUtil.class);

  public static void success(
      String methodName, long startTime, String uid, String additionalNotes) {
    long timeTaken = System.currentTimeMillis() - startTime;
    if (timeTaken < 50) {
      if (log.isDebugEnabled()) {
        success(log::debug, methodName, timeTaken, uid, additionalNotes);
      }
    } else {
      success(log::info, methodName, timeTaken, uid, additionalNotes);
    }
  }

  protected static void success(
      BiConsumer<String, Object[]> logger,
      String methodName,
      long timeTaken,
      String uid,
      String additionalNotes) {

    logger.accept(
        "APP_STATS method={} status=OK, timeTaken={}, uid={}, {}",
        new String[] {
          methodName, "" + timeTaken, uid, additionalNotes != null ? additionalNotes : ""
        });
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
