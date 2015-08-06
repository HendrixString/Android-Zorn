package com.hendrix.zorn.workers;

/**
 * callback interface for {@link IWorker}
 *
 * @see IWorker
 */
public interface WorkerObserver {
    /**
     * process complete callback
     *
     * @param worker the completed process
     */
    void onWorkerComplete(IWorker worker);
    /**
     * process progress callback
     *
     * @param worker the progressed process
     */
    void onWorkerProgress(IWorker worker);
    /**
     * process error callback
     *
     * @param worker the erroneous process
     */
    void onWorkerError(IWorker worker);
}
