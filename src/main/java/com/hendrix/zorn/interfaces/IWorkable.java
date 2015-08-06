package com.hendrix.zorn.interfaces;

import com.hendrix.zorn.workers.WorkerObserver;

import java.util.concurrent.ExecutorService;

/**
 * @author Tomer Shalev
 */
@SuppressWarnings("UnusedDeclaration")
public interface IWorkable extends IWork {

    /**
     * stop processing the item
     */
    void stop();

    /**
     * process the item
     *
     */
    void process();

    /**
     * process the item
     *
     * @param workerObserver callback interface for a process
     */
    void process(WorkerObserver workerObserver);

    /**
     * process the item with multithreaded capabilities
     *
     * @param workerObserver    callback interface for a process
     * @param es                the {@code ExecutorService} to interact with
     */
    void process(WorkerObserver workerObserver, ExecutorService es);

    /**
     * notify completion. this should force the worker to complete it's work and to
     * notify it's listener.
     *
     */
    void notifyComplete();

    /**
     * notify error. this should force the worker to complete it's work and to
     * notify it's listener.
     *
     */
    void notifyError();

    /**
     * notify progress to it's listener.
     *
     */
    void notifyProgress();


}
