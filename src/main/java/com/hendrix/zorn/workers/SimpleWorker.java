package com.hendrix.zorn.workers;

import android.util.Log;

import com.hendrix.zorn.interfaces.IWork;

/**
 * Simple {@link IWorker} implementation.
 * receives it's work in the constructor as an {@link IWork} implementation.
 * the {@link IWork} is presumed to be synchronous, and can be ran on async by the
 * {@link SimpleWorker}
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("UnusedDeclaration")
public class SimpleWorker extends AbstractWorker {
    private IWork _work = null;

    /**
     *
     * @param work          {@link IWork} that will do the work.
     * @param id            {@code identifier} (Optional)
     * @param priorityKey   priority (Optional)
     */
    public SimpleWorker(IWork work, String id, int priorityKey) {
        super(id, priorityKey);

        if(work == null)
            throw new NullPointerException("work is null!!");

        _work = work;
    }

    /**
     *
     * @param work          {@link IWork} that will do the work.
     * @param id            {@code identifier} (Optional)
     */
    public SimpleWorker(IWork work, String id) {
        this(work, id, 0);
    }

    /**
     *
     * @param work          {@link IWork} that will do the work.
     */
    public SimpleWorker(IWork work) {
        this(work, "Anonymous Worker");
    }

    @Override
    public void work() {
        _work.work();

        System.out.println("process.work() id: " + _id);
/*
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }

    /**
     * stop processing the item
     */
    @Override
    public void stop() {
        Log.i(ZORN_WORKER_TAG, "stop() is not implemented in your worker!!");
    }

    /**
     * called when work has progressed, if you do not prefer to use {@link WorkerObserver}
     */
    @Override
    protected void onProgress() {
    }

    /**
     * called when work is complete, if you do not prefer to use {@link WorkerObserver}
     */
    @Override
    protected void onComplete() {
    }
}
