package com.hendrix.zorn.managers;

import com.hendrix.zorn.interfaces.IDisposable;
import com.hendrix.zorn.workers.IWorker;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public interface IWorkerManager extends IDisposable {
    /**
     *	pause the process manager
     */
    void pause();

    /**
     * resume the process manager
     */
    void resume();

    /**
     * start the process manager
     */
    void start();

    /**
     * Stop the process manager
     */
    void stop();

    /**
     * retry failed processes
     */
    void retry();

    /**
     * signals completion, or more specifically
     * when status went from {@code WORKING} into {@code IDLE}.
     *
     */
    void notifyComplete();

    /**
     * signals progress to listener
     *
     * @param worker the process that made progress
     */
    void notifyProgress(IWorker worker);

    /**
     * signals Error
     *
     * @param res the error description
     */
    void notifyError(WorkerManagerErrorInfo res);

    /**
     * enqueue a process into the priority queue
     *
     * @param element A processable element
     */
    void enqueue(IWorker element);

    /**
     * get a finished process by it's id
     *
     * @param id {@code id} of the process.
     *
     * @return the process
     *
     */
    IWorker getFinishedWorker(String id);

    /**
     * get finished workers
     *
     * @return A map of {@code IWorker} elements
     */
    Map<String, IWorker> getFinishedWorkers();

    /**
     * Get the failed workers
     *
     * @return A vector of {@code IWorker} elements
     */
    Collection<IWorker> getFailedWorkers();

    /**
     * @return The status object
     */
    WorkerManagerStatus getStatusInfo();

    /**
     * @return the number of overall workers = pending + running + failed
     */
    int numWorkers();

    /**
     * @return flag if process manager status is {@code STATUS_STOP}
     */
    boolean isStopped();

    /**
     * @return flag if process manager status is {@code STATUS_PAUSE}
     */
    boolean isPaused();

    /**
     * @return flag if process manager status is {@code (STATUS_WORKING || STATUS_IDLE)}
     */
    boolean isRunning();

    /**
     * @return flag if process manager status is {@code STATUS_WORKING}
     */
    boolean isWorking();

    /**
     * @return flag if process manager status is {@code STATUS_IDLE}
     */
    boolean isIdle();

    /**
     * @return flag if process manager status is {@code STATUS_READY}
     */
    boolean isReady();

    /**
     *
     * @return the id of the Process Manager
     */
    String getId();
}
