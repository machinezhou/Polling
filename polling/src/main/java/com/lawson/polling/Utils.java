package com.lawson.polling;

import android.support.annotation.NonNull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static java.lang.Thread.MIN_PRIORITY;

/**
 * Created by lawson on 16/3/28.
 */
final class Utils {

  /**
   * tic tac message
   */
  public static final int MESSAGE_TIC_TAC = 0x110;

  /**
   * reset message
   */
  public static final int MESSAGE_CLIENT = 0x111;

  /**
   * window changed message
   */
  public static final int MESSAGE_CLIENT_DESTROY = 0x112;
  public static final int MESSAGE_CLIENT_PAUSE = 0x113;
  public static final int MESSAGE_CLIENT_RESUME = 0x114;

  /**
   * http failure message
   */
  public static final int MESSAGE_CLIENT_HTTP_FAILURE = 0x115;

  static ExecutorService defaultTaskExecutor() {
    return Executors.newCachedThreadPool(new ThreadFactory() {
      @Override public Thread newThread(@NonNull final Runnable r) {
        return new Thread(new Runnable() {
          @Override public void run() {
            Thread.currentThread().setPriority(MIN_PRIORITY);
            r.run();
          }
        }, Polling.IDLE_THREAD_NAME);
      }
    });
  }
}
