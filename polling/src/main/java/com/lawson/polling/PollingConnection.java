package com.lawson.polling;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import static com.lawson.polling.Utils.MESSAGE_CLIENT;
import static com.lawson.polling.Utils.MESSAGE_CLIENT_DESTROY;

/**
 * Created by lawson on 16/3/26.
 */
public class PollingConnection {

  public static final String TAG = "PollingConnection";
  private Messenger sMessenger;

  private Messenger cMessenger;
  private PollingTask task;
  private long interval;
  private boolean needUnbind = false;

  private final Handler handler = new Handler(Looper.myLooper()) {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (msg.what == MESSAGE_CLIENT_DESTROY) {
        //just in case of service die of unexpected
        needUnbind = false;
      }
    }
  };

  private ServiceConnection connection = new ServiceConnection() {

    public void onServiceConnected(ComponentName name, IBinder service) {
      needUnbind = true;
      sMessenger = new Messenger(service);
      cMessenger = new Messenger(handler);
      try {
        sendTask(MESSAGE_CLIENT);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }

    public void onServiceDisconnected(ComponentName name) {
      sMessenger = null;
      needUnbind = false;
    }
  };

  public ServiceConnection getConnection() {
    return connection;
  }

  public boolean getConnectionStatus() {
    return needUnbind;
  }

  public PollingTask getTask() {
    return task;
  }

  public void setTask(PollingTask task) {
    this.task = task;
  }

  public void setInterval(long interval) {
    this.interval = interval;
  }

  public long getInterval() {
    return interval;
  }

  public void changeTask(PollingTask task) {
    setTask(task);
    try {
      sendTask(MESSAGE_CLIENT);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  public void sendTask(int tag) throws RemoteException {
    if (task == null) {
      throw PollingInterruption.unexpectedInterruption("task must not be null here");
    }
    if (!getConnectionStatus()) {
      /** resume first */
      return;
    }
    Message message = Message.obtain(null, tag);
    message.replyTo = cMessenger;
    Bundle bundle = new Bundle();
    bundle.putLong(PollingService.MESSAGE_INTERVAL, getInterval());
    message.obj = getTask();
    message.setData(bundle);
    sMessenger.send(message);
  }

  public void sendFailure(Throwable throwable, int tag) throws RemoteException {
    Message message = Message.obtain(null, tag);
    message.replyTo = cMessenger;
    Bundle bundle = new Bundle();
    message.obj = throwable;
    message.setData(bundle);
    sMessenger.send(message);
  }
}
