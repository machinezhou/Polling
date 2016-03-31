package com.lawson.polling;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import static com.lawson.polling.Utils.MESSAGE_CLIENT_HTTP_FAILURE;
import static com.lawson.polling.Utils.MESSAGE_CLIENT_PAUSE;
import static com.lawson.polling.Utils.MESSAGE_CLIENT_RESUME;

/**
 * Created by lawson on 16/3/25.
 */
public class Polling {

  public static final String TAG = "Polling";
  static final String THREAD_PREFIX = "Polling-";
  static final String IDLE_THREAD_NAME = THREAD_PREFIX + "Idle";

  final Context context;
  final Intent intent;
  private final PollingConnection connection;

  private Polling(Context context, Intent intent, PollingConnection connection, long interval) {
    this.context = context;
    this.intent = intent;
    this.connection = connection;
    connection.setInterval(interval);
  }

  public void start(PollingTask task) {
    connection.setTask(task);
    context.bindService(intent, connection.getConnection(), Context.BIND_AUTO_CREATE);
  }

  public void stop() {
    if (connection.getConnectionStatus()) {
      context.unbindService(connection.getConnection());
    }
  }

  public void pause() {
    try {
      connection.sendTask(MESSAGE_CLIENT_PAUSE);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  public void resume() {
    try {
      connection.sendTask(MESSAGE_CLIENT_RESUME);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  public void httpFailure(Throwable throwable) {
    try {
      connection.sendFailure(throwable, MESSAGE_CLIENT_HTTP_FAILURE);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  //// TODO: 16/3/31 change interval and task from client in the middle of polling

  public static class Builder {
    private long interval;
    private Context context;
    private Intent intent;
    private PollingConnection connection;

    public Builder(Context context) {
      if (context == null) {
        throw new IllegalArgumentException("Context must not be null.");
      }
      this.context = context.getApplicationContext();
    }

    public Builder interval(long interval) {
      this.interval = interval;
      return this;
    }

    public Polling build() {
      this.connection = new PollingConnection();
      this.intent = new Intent(context, PollingService.class);
      return new Polling(context, intent, connection, interval);
    }
  }
}
