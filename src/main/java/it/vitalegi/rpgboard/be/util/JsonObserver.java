package it.vitalegi.rpgboard.be.util;

import io.reactivex.MaybeObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import it.vitalegi.rpgboard.be.MainVerticle;
import it.vitalegi.rpgboard.be.logging.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonObserver implements MaybeObserver<Object> {
  Logger log = LoggerFactory.getLogger(JsonObject.class);
  Message<JsonObject> msg;
  String methodName;
  long startTime;

  protected JsonObserver(Message<JsonObject> msg, String methodName, long startTime) {
    this.msg = msg;
    this.methodName = methodName;
    this.startTime = startTime;
  }

  public static JsonObserver init(Message<JsonObject> msg, String methodName) {
    return new JsonObserver(msg, methodName, System.currentTimeMillis());
  }

  @Override
  public void onSubscribe(@NonNull Disposable d) {}

  @Override
  public void onSuccess(@NonNull Object entry) {
    LogUtil.success(methodName, startTime, getUserId(msg), null);
    if (entry instanceof List) {
      msg.reply(VertxUtil.jsonMap((List<?>) entry));
      return;
    }
    if (entry instanceof Boolean) {
      msg.reply(entry);
      return;
    }
    msg.reply(JsonObject.mapFrom(entry));
  }

  @Override
  public void onError(@NonNull Throwable failure) {
    LogUtil.failure(methodName, startTime, getUserId(msg), null, failure);
    log.error(failure.getMessage(), failure);
    msg.fail(
        500,
        new JsonObject()
            .put("error", failure.getClass().getName())
            .put("description", failure.getMessage())
            .toString());
  }

  @Override
  public void onComplete() {
    LogUtil.success(methodName, startTime, getUserId(msg), "completed with empty result");
    msg.reply(new JsonObject());
  }

  protected String getUserId(Message<JsonObject> msg) {
    return msg.headers().get(MainVerticle.UID);
  }
}
