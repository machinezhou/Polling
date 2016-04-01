# Polling
It's a handy library for basic background polling.
(用于基本的后台轮询)


# Features

* Polling data to server (轮询)
* run your own task synchronously or asynchronously (同步或异步执行你自己的任务)
* Interval will change according to activity lifecycle, common exception and network change(轮询间隔会根据生命周期，异常以及网络信号变化而作出变化)

# Usage

```
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
    MyTask myTask = new MyTask(polling);

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
```

# Next things I'm working on:
1. Catch the exception from your async task which wrapped with other HTTP clients(从其他网络框架捕获请求异常和错误)
2. Customize interval changing policy(自定义轮询间隔策略)



