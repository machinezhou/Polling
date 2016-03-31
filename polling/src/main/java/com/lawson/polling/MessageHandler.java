package com.lawson.polling;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.lawson.polling.Utils.MESSAGE_CLIENT;
import static com.lawson.polling.Utils.MESSAGE_CLIENT_DESTROY;
import static com.lawson.polling.Utils.MESSAGE_CLIENT_HTTP_FAILURE;
import static com.lawson.polling.Utils.MESSAGE_CLIENT_PAUSE;
import static com.lawson.polling.Utils.MESSAGE_CLIENT_RESUME;
import static com.lawson.polling.Utils.MESSAGE_TIC_TAC;

/**
 * Created by lawson on 16/3/29.
 */
final class MessageHandler extends Handler {

  private InterruptionHandler interruptionHandler;
  private final ExecutorService pool = Utils.defaultTaskExecutor();
  private PollingTask task;
  private long exp = Long.MIN_VALUE;
  private long interval;

  public MessageHandler(Looper looper, InterruptionHandler interruptionHandler) {
    super(looper);
    this.interruptionHandler = interruptionHandler;
  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored") @Override
  public void handleMessage(Message msg) {
    try {
      switch (msg.what) {
        case MESSAGE_TIC_TAC:
          if (checkExp()) {
            Future<?> wrapper = pool.submit(task);
            try {
              wrapper.get();
            } catch (InterruptedException | ExecutionException e) {
              throw PollingInterruption.unexpectedInterruption(e.getMessage());
            }
            sendEmptyMessageDelayed(MESSAGE_TIC_TAC, interval);
          }
          break;
        case MESSAGE_CLIENT:
          task = (PollingTask) msg.obj;
          interruptionHandler.handleClientMessage(msg);
          sendEmptyMessage(MESSAGE_TIC_TAC);
          break;
        case MESSAGE_CLIENT_RESUME:
          throw PollingInterruption.windowChange("window resume",
              PollingInterruption.WINDOW_RESUME);
        case MESSAGE_CLIENT_PAUSE:
          throw PollingInterruption.windowChange("window pause", PollingInterruption.WINDOW_STOP);
        case MESSAGE_CLIENT_DESTROY:
          throw PollingInterruption.windowChange("window destroy",
              PollingInterruption.WINDOW_REALLY_STOP);
        case MESSAGE_CLIENT_HTTP_FAILURE:
          Throwable throwable = (Throwable) msg.obj;
          throw PollingInterruption.httpInterruption(throwable.getMessage(), throwable);
        default:
          throw PollingInterruption.unexpectedInterruption("message type doesn't match");
      }
    } catch (PollingInterruption e) {
      interruptionHandler.handleInterruption(e);
    }
  }

  private boolean checkExp() {
    if (exp - interval > 0) {
      exp -= interval;
    } else if (exp != Long.MIN_VALUE) {
      return false;
    }
    return true;
  }

  public void replaceInterval(long interval) {
    this.interval = interval;
  }

  public void replaceInterval(long interval, long exp) {
    this.interval = interval;
    this.exp = exp;
  }

  public void stop() {
    pool.shutdownNow();
    exp = -1;
  }
}
