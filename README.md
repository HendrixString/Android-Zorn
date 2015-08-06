# Android-Zorn
Asynchronous Workers and Worker Managers for Android.

### Explanation
* use `Worker` API as a replacement for `AsyncTask`
* workers run on background threads and return callbacks on the main(calling) thread.
* automate a batch of workers with two worker managers:
  * `PriorityWorkerManager` - workers are processed according to their priority both serially or unbounded.
  * `TopologicalWorkerManager` - workers are processed according to a binary topological relation and order.

### Using a Worker
Simply anonymously instantiate or extend `AbstractWorker`

```
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
