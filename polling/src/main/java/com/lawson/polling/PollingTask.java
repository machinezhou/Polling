package com.lawson.polling;

import java.util.concurrent.Callable;

import static com.lawson.polling.PollingInterruption.unexpectedInterruption;

/**
 * Created by lawson on 16/3/28.
 */
public abstract class PollingTask implements Callable<Object> {

  protected Polling polling;
  InterruptionHandler handler;

  public PollingTask(Polling polling, InterruptionHandler handler) {
    if (polling == null) {
      throw new IllegalArgumentException("polling must not be null.");
    }
    this.polling = polling;
    this.handler = handler == null ? InterruptionHandler.DEFAULT : handler;
  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored") @Override public Object call() {
    try {
      runTask();
    } catch (PollingInterruption e) {
      /** handle sync failure to adjust polling */
      Throwable cause = handler.handleInterruption(e);
      throw cause == e ? e : unexpectedInterruption(cause.getMessage());
    }
    return null;
  }

  protected abstract void runTask();
}
