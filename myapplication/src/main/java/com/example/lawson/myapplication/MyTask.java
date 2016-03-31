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
 * Created by lawson on 16/3/26.
 */
final class MyTask extends PollingTask {

  public static final String TAG = "MyTask";

  public MyTask(Polling polling) {
    super(polling, null);
  }

  @Override public void runTask() {

    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url("http://httpbin.org/delay/2").build();

    client.newCall(request).enqueue(new Callback() {
      @Override public void onFailure(Request request, IOException throwable) {
        Log.e(TAG, throwable.getMessage());
        /** handle async failure to adjust polling */
        polling.httpFailure(throwable);
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
