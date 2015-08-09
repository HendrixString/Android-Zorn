package com.hendrix.zorn;

import com.hendrix.zorn.managers.PriorityWorkerManager;
import com.hendrix.zorn.managers.TopologicalWorkerManager;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Use this {@code factory} class to find out about the various {@link com.hendrix.zorn.managers.IWorkerManager} implementations.
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("unused")
public class Zorn {

    static public ThreadPoolExecutor defaultExecutorService;

    static {
        setupExecutor();
    }

    private Zorn() {
    }

    /**
     * get a {@code PriorityWorkerManager}.
     *
     * @return a {@link PriorityWorkerManager} instance
     */
    static public PriorityWorkerManager newPriorityWorkerManager() {
        return new PriorityWorkerManager();
    }

    /**
     * get a {@code TopologicalWorkerManager.Builder}.
     *
     * @return a {@link TopologicalWorkerManager.Builder} instance.
     *
     * @see com.hendrix.zorn.managers.TopologicalWorkerManager.Builder
     */
    static public TopologicalWorkerManager.Builder newTopologicalWorkerManager() {
        return new TopologicalWorkerManager.Builder();
    }

    /**
     * setup the thread executor
     */
    static private void setupExecutor()
    {
        int count_cpu           = Runtime.getRuntime().availableProcessors();

        final ThreadFactory pmThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @SuppressWarnings("NullableProblems")
            public Thread newThread(Runnable r) {
                return new Thread(r, "Zorn free worker, worker #" + mCount.getAndIncrement());
            }
        };

        defaultExecutorService  = new ThreadPoolExecutor(count_cpu + 1, count_cpu*2 + 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), pmThreadFactory);
    }


}
