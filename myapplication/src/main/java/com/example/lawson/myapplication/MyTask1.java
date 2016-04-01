package com.example.lawson.myapplication;

import android.util.Log;
import com.lawson.polling.Polling;
import com.lawson.polling.PollingTask;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;

/**
 * Created by lawson on 16/4/1.
 */
public class MyTask1 extends PollingTask {

  public static final String TAG = "MyTask";

  public MyTask1(Polling polling) {
    super(polling, null);
  }

  @Override public void runTask() {

    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url("http://192.168.1.88").build();

    client.newCall(request).enqueue(new Callback() {
      @Override public void onFailure(Request request, IOException throwable) {
        Log.e(TAG, throwable.getMessage());
        /** handle async failure to adjust polling */
        polling.httpFailure(throwable);

        /** change current task when it fail */
        polling.changeTask(new MyTask(polling));
      }

      @Override public void onResponse(Response response) throws IOException {
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
          System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }

        System.out.println(response.body().string());
      }
    });
  }
}
