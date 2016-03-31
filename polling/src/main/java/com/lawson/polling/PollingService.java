package com.lawson.polling;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import static com.lawson.polling.Utils.MESSAGE_CLIENT_DESTROY;

/**
 * Created by lawson on 16/3/25.
 */
public class PollingService extends Service
    implements NetworkReceiver.NetworkChangeListener, InterruptionHandler {

  public static String TAG = "PollingService";
  public static String MESSAGE_INTERVAL = "polling_interval";

  private Messenger sm;
  private Messenger cm;
  MessageHandler messageHandler;
  private PowerManager.WakeLock wakeLock;

  private long initial_seed;

  private void setInitialSeed(long seed) {
    if (initial_seed == 0) {
      initial_seed = seed;
    }
  }

  @Override public void onCreate() {
    super.onCreate();
    messageHandler = new MessageHandler(Looper.myLooper(), this);
    sm = new Messenger(messageHandler);
    NetworkReceiver.register(this);
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PollingService.class.getName());
    wakeLock.acquire();
  }

  @Override public void onDestroy() {
    try {
      cm.send(Message.obtain(null, MESSAGE_CLIENT_DESTROY));
    } catch (RemoteException e) {
      e.printStackTrace();
    } finally {
      wakeLock.release();
      messageHandler.stop();
      NetworkReceiver.unRegister(this);
    }
    super.onDestroy();
  }

  @Override public void onLowMemory() {
    super.onLowMemory();
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return sm.getBinder();
  }

  @Override public void onRebind(Intent intent) {
    super.onRebind(intent);
  }

  @Override public void onNetworkChange(NetworkInfo info) {
    adjustPollingInterval(info);
  }

  /**
   * take from picasso for interval change
   */
  void adjustPollingInterval(NetworkInfo info) {
    if (info == null || !info.isConnectedOrConnecting()) {
      messageHandler.replaceInterval(ValidPeriodPolicy.disconnectInterval(initial_seed),
          ValidPeriodPolicy.exp(initial_seed));
      return;
    }
    switch (info.getType()) {
      case ConnectivityManager.TYPE_MOBILE:
        switch (info.getSubtype()) {
          case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
          case TelephonyManager.NETWORK_TYPE_HSPAP:
          case TelephonyManager.NETWORK_TYPE_EHRPD:
            messageHandler.replaceInterval(ValidPeriodPolicy.lteInsteadInterval(initial_seed));
            break;
          case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
          case TelephonyManager.NETWORK_TYPE_CDMA:
          case TelephonyManager.NETWORK_TYPE_EVDO_0:
          case TelephonyManager.NETWORK_TYPE_EVDO_A:
          case TelephonyManager.NETWORK_TYPE_EVDO_B:
            messageHandler.replaceInterval(ValidPeriodPolicy.umtsInsteadInterval(initial_seed));
            break;
          case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
          case TelephonyManager.NETWORK_TYPE_EDGE:
          default:
            messageHandler.replaceInterval(ValidPeriodPolicy.gprsInsteadInterval(initial_seed));
        }
        break;
      case ConnectivityManager.TYPE_WIFI:
      case ConnectivityManager.TYPE_WIMAX:
      case ConnectivityManager.TYPE_ETHERNET:
      default:
        messageHandler.replaceInterval(initial_seed);
    }
  }

  @Override public Throwable handleInterruption(PollingInterruption cause) {
    switch (cause.getKind()) {
      case HTTP:
        messageHandler.replaceInterval(ValidPeriodPolicy.httpInterruptionInterval(initial_seed),
            ValidPeriodPolicy.exp(initial_seed));
        break;
      case WINDOW:
        switch (cause.getWindowType()) {
          case PollingInterruption.WINDOW_REALLY_STOP:
            messageHandler.replaceInterval(ValidPeriodPolicy.windowDestroyInterval(initial_seed));
            break;
          case PollingInterruption.WINDOW_RESUME:
            messageHandler.replaceInterval(initial_seed);
            break;
          case PollingInterruption.WINDOW_STOP:
            messageHandler.replaceInterval(ValidPeriodPolicy.windowPauseInterval(initial_seed));
            break;
          default:
            throw new AssertionError("unexpected window change type");
        }
        break;
      case UNEXPECTED:
        messageHandler.stop();
        break;
      default:
        break;
    }
    return cause;
  }

  @Override public void handleClientMessage(Message msg) {
    setInitialSeed(msg.getData().getLong(MESSAGE_INTERVAL));
    if (initial_seed == 0) {
      throw new AssertionError("initial seed must be non zero");
    }
    cm = msg.replyTo;
    messageHandler.replaceInterval(initial_seed);
  }
}
