package com.lawson.polling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lawson on 16/3/26.
 */
public final class NetworkReceiver extends BroadcastReceiver {
  private static final List<NetworkChangeListener> listener = new ArrayList<>();
  private static final String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

  @Override public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(NET_CHANGE_ACTION)) {
      if (listener.size() > 0) {
        ConnectivityManager manager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        for (NetworkChangeListener handler : listener) {
          handler.onNetworkChange(manager.getActiveNetworkInfo());
        }
      }
    }
  }

  public static void register(NetworkChangeListener l) {
    if (!listener.contains(l)) {
      listener.add(l);
    }
  }

  public static void unRegister(NetworkChangeListener l) {
    if (listener.contains(l)) {
      listener.remove(l);
    }
  }

  public static void clear() {
    if (listener.size() != 0) {
      listener.clear();
    }
  }

  public interface NetworkChangeListener {
    void onNetworkChange(NetworkInfo info);
  }
}
