package com.hendrix.zorn.managers;

import com.hendrix.zorn.workers.IWorker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * a further implementation of {@link AbstractWorkerManager}, that handles
 * the {@code getRunningWorkers(), getFailedWorkers(), getFinishedWorkers()}
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("UnusedDeclaration")
abstract public class BaseAbstractWorkerManager extends AbstractWorkerManager {
    /**
     * the list that holds currently running processes
     */
    private ArrayList<IWorker> _runningWorkers = null;
    /**
     * a map that holds finished processes
     */
    private Map<String, IWorker> _finishedWorkers = null;
    /**
     * a map that holds failed processes
     */
    private ArrayList<IWorker> _failedWorkers = null;

    /**
     * a mutable flag indicating if finished processes are stored and can later be retrieved with the api
     */
    private boolean _storeFinishedWorkers =	true;

    /**
     * {@inheritDoc}
     *
     */
    public BaseAbstractWorkerManager(String id) {
        super(id);

        if(_storeFinishedWorkers)
            _finishedWorkers    =   new HashMap<>();

        _runningWorkers         =   new ArrayList<>();
        _failedWorkers          =   new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     *
     */
    public BaseAbstractWorkerManager() {
        this(null);
    }

    /**
     * dispose the object
     */
    @Override
    public void dispose() {
        super.dispose();

        _finishedWorkers = null;
        _failedWorkers   = null;
        _runningWorkers  = null;
    }

    /**
     * Get the failed workers
     *
     * @return A vector of {@code IWorker} elements
     */
    @Override
    public Collection<IWorker> getFailedWorkers() {
        return _failedWorkers;
    }

    /**
     * signals completion, or more specifically
     * when status went from {@code WORKING} into {@code IDLE}.
     */
    @Override
    public void notifyComplete() {
        if(_listener != null)
            _listener.onComplete(this);
    }
    /**
     * signals progress to listener
     *
     * @param worker the process that made progress
     */
    @Override
    public void notifyProgress(IWorker worker) {
        if(_listener != null)
            _listener.onProgress(worker.getId());
    }
    /**
     * signals Error
     *
     * @param res the error description
     */
    @Override
    public void notifyError(WorkerManagerErrorInfo res) {
        if(_listener != null)
            _listener.onError(res);
    }

    /**
     * get a finished process by it's id
     *
     * @param id {@code id} of the process.
     * @return the process
     */
    @Override
    public IWorker getFinishedWorker(String id) {
        return _finishedWorkers.get(id);
    }

    /**
     * get finished workers
     *
     * @return A map of {@code IWorker} elements
     */
    @Override
    public Map<String, IWorker> getFinishedWorkers() {
        return _finishedWorkers;
    }

    /**
     * a mutable flag indicating if finished processes are stored and can later be retrieved with the api.
     *
     * @param flag {@code true/false}
     */
    public void storeFinishedWorkers(boolean flag) {
        _storeFinishedWorkers = flag;
    }

    /**
     * @return the number of overall workers = pending + running + failed
     *
     */

    @Override
    public int numWorkers() {
        return getRunningWorkers().size() + getFailedWorkers().size() + sizePendingWorkers();
    }

    /**
     * would ou like to store finished workers
     *
     * @return {@code true/false}
     */
    @Override
    protected boolean storeFinishedWorkers() {
        return _storeFinishedWorkers;
    }

    /**
     * get currently running workers
     *
     * @return a {@link Collection} of {@link IWorker}
     */
    @Override
    protected Collection<IWorker> getRunningWorkers() {
        return _runningWorkers;
    }

}
