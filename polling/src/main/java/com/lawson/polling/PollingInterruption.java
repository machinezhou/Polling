package com.lawson.polling;

import android.net.NetworkInfo;

/**
 * Created by lawson on 16/3/25.
 */
public class PollingInterruption extends RuntimeException {

  public static PollingInterruption networkInterruption(String message, NetworkInfo info) {
    return new PollingInterruption(message, Kind.NETWORK, -1, info, null);
  }

  public static PollingInterruption httpInterruption(String message, Throwable throwable) {
    return new PollingInterruption(message, Kind.HTTP, -1, null, throwable);
  }

  public static PollingInterruption windowChange(String message, int type) {
    return new PollingInterruption(message, Kind.WINDOW, type, null, null);
  }

  public static PollingInterruption unexpectedInterruption(String message) {
    return new PollingInterruption(message, Kind.UNEXPECTED, -1, null, null);
  }

  public enum Kind {
    /** network change */
    NETWORK,
    /** http fail */
    HTTP,
    /**
     * window switch
     */
    WINDOW,
    UNEXPECTED
  }

  private final Kind kind;
  private final int windowType;
  private final NetworkInfo networkInfo;

  public static final int WINDOW_RESUME = 0;
  public static final int WINDOW_STOP = 1;
  public static final int WINDOW_REALLY_STOP = 2;

  PollingInterruption(String message, Kind kind, int type, NetworkInfo info, Throwable exception) {
    super(message, exception);
    this.kind = kind;
    this.windowType = type;
    this.networkInfo = info;
  }

  public Kind getKind() {
    return kind;
  }

  public int getWindowType() {
    return windowType;
  }

  public NetworkInfo getNetworkInfo() {
    return networkInfo;
  }
}
