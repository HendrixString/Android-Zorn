# Android-Zorn
Asynchronous Workers and Worker Managers for Android.

## How to use
simply fork or download the project, you can also download and create `.aar` file yourself.

### Notable Features
* use `Worker` API as a replacement for `AsyncTask`
* workers run on background threads and return callbacks on the main(calling) thread.
* automate a batch of workers with two worker managers:
  * `PriorityWorkerManager` - workers are processed according to their priority both serially or unbounded.
  * `TopologicalWorkerManager` - workers are processed according to a binary topological relation and order.

### Using a Worker
#### 1. Simply anonymously instantiate `AbstractWorker`

```
AbstractWorker worker = new AbstractWorker() {
    @Override
    protected void onProgress() {
        // runs on the calling/main thread
    }

    @Override
    protected void onComplete() {
        // runs on the calling/main thread
    }

    @Override
    public void work() {
        // here you put work to be done in a background thread
    }
};

// run async
worker.process();

```
#### 2. Simply extend `AbstractWorker`

```
MyWorker worker = new MyWorker();

worker.process(new WorkerObserver() {
    @Override
    public void onWorkerComplete(IWorker worker) {
        // runs on the calling/main thread
    }

    @Override
    public void onWorkerProgress(IWorker worker) {
        // runs on the calling/main thread
    }

    @Override
    public void onWorkerError(IWorker worker) {
        // runs on the calling/main thread
    }
});

```

#### 3. use `SimpleWorker` with a `IWork` object (like `Runnable`)

```
SimpleWorker sw = new SimpleWorker(new IWork() {
    @Override
    public void work() {
        // here you put work to be done in a background thread
    }
});

sw.process();

```

####  notes
* `IWorker.process(..)` method also have an overloaded version where one can pass `ExecutorService`
* `IWorker` supports many more methods and ideas. I did not go through all.
* in the future, I will add support for Java native `FutureTask` and `Callable` so worker can have cancelling feature.

### Using a Worker Manager
Worker managers support a lot of functionality such as pause, start, stop etc..

#### Priority Worker Manager.
```
pm = new PriorityWorkerManager("myId");
// serial mode
pm.setExecutionMode(AbstractWorkerManager.EXECUTION_MODE.SERIAL);

IWorker worker = null;
String  id;
int     priority;

for(int ix = 0; ix < 20; ix++) {
    id        = String.valueOf(ix);
    priority  = ix;
    
    worker    = new TestWorker(id, priority);

    pm.enqueue(worker);
}

pm.setListener(new WorkerManagerObserver() {
    @Override
    public void onComplete(IWorkerManager wm) {
        // runs on the calling/main thread
    }

    @Override
    public void onProgress(String id) {
        // runs on the calling/main thread
    }

    @Override
    public void onError(WorkerManagerErrorInfo err) {
        // runs on the calling/main thread
    }
});

pm.start();
```

#### Topological Worker Manager.
Use the `TopologicalWorkerManager.Builder` or `Zorn.newTopologicalWorkerManager()`
to create a worker manager that takes into account a directed binary relation among workers.
```
TestWorker a1 = new TestWorker("a1");
TestWorker a2 = new TestWorker("a2");
TestWorker a3 = new TestWorker("a3");
TestWorker a4 = new TestWorker("a4");
TestWorker a5 = new TestWorker("a5");

TopologicalWorkerManager tm = new TopologicalWorkerManager.Builder().id("topological_test")
                                                          .listener(this)
                                                          .before(a1, a3)
                                                          .before(a2, a3)
                                                          .after(a4, a3)
                                                          .after(a5, a4)
                                                          .build();
                                                          
tm.setListener(...);

tm.start();                                                          
```



### Dependencies
* [`Erdos`](https://github.com/HendrixString/Erdos-Graph-framework)

### Terms
* completely free source code. [Apache License, Version 2.0.](http://www.apache.org/licenses/LICENSE-2.0)
* if you like it -> star or share it with others

### Contact Author
* [tomer.shalev@gmail.com](tomer.shalev@gmail.com)
* [Google+ TomershalevMan](https://plus.google.com/+TomershalevMan/about)
* [Facebook - HendrixString](https://www.facebook.com/HendrixString)
