package com.lawson.polling;

import android.os.Message;

/**
 * Created by lawson on 16/3/25.
 */
public interface InterruptionHandler {
  Throwable handleInterruption(PollingInterruption cause);

  void handleClientMessage(Message msg);

  InterruptionHandler DEFAULT = new InterruptionHandler() {
    @Override public Throwable handleInterruption(PollingInterruption cause) {
      return cause;
    }

    @Override public void handleClientMessage(Message msg) {

    }
  };
}
