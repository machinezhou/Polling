package com.example.lawson.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.lawson.polling.Polling;

public class MainActivity extends AppCompatActivity {

  Polling polling;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    begin();
  }

  @Override protected void onResume() {
    super.onResume();
    polling.resume();
  }

  private void begin() {
    polling = new Polling.Builder(getApplicationContext()).interval(6000).build();
    MyTask1 myTask = new MyTask1(polling);

    polling.start(myTask);
  }

  @Override protected void onPause() {
    super.onPause();
    polling.pause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    polling.stop();
  }
}
