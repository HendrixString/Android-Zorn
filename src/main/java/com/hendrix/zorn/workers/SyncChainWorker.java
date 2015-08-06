package com.hendrix.zorn.workers;

import com.hendrix.zorn.interfaces.IWork;

import java.util.ArrayList;

/**
 * an {@link AbstractWorker} that runs other synchronous {@link IWork} workers sequentially like a chain.
 * it only guarantees to start them sequentially on the same {@link java.util.concurrent.ExecutorService}
 * as it is running on and therefore all will run on the same {@link Thread}. if, the code inside the {@code work()}
 * is async itself, than this {@link SyncChainWorker} will complete before it's children have completed with high probability.
 * Therefore, use it with synchronous code for maximum results.
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("UnusedDeclaration")
public class SyncChainWorker extends AbstractWorker {
    private ArrayList<IWork> _list_workers = null;

    /**
     * constructor with parameters that are essential when the {@link IWorker}
     * is running inside a {@link com.hendrix.zorn.managers.IWorkerManager}
     *
     * @param id          identifier of worker (Optional).
     * @param priorityKey the priority of the worker (Optional).
     */
    public SyncChainWorker(String id, int priorityKey) {
        super(id, priorityKey);
    }

    /**
     * constructor with parameters that are essential when the {@link IWorker}
     * is running inside a {@link com.hendrix.zorn.managers.IWorkerManager}
     *
     * @param id identifier of worker (Optional).
     */
    public SyncChainWorker(String id) {
        super(id);
    }

    /**
     * empty constructor.
     */
    public SyncChainWorker() {
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

    private ArrayList<IWork> get_list_workers() {
        return _list_workers==null ? new ArrayList<IWork>() : _list_workers;
    }

    /**
     * add a new {@link com.hendrix.zorn.interfaces.IWork} into the working chain.
     *
     * @param worker a {@link com.hendrix.zorn.interfaces.IWork}
     */
    public void add(AbstractWorker worker) {
        get_list_workers().add(worker);
    }

    /**
     * work to be done.
     */
    @Override
    public void work() {
        for (IWork worker : _list_workers) {
            worker.work();
        }

    }

}
