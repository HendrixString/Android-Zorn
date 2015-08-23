package com.hendrix.zorn.managers;

import com.hendrix.zorn.workers.IWorker;
import com.hendrix.zorn.workers.WorkerObserver;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract {@link IWorkerManager} base pre implementation
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("UnusedDeclaration")
abstract public class AbstractWorkerManager implements IWorkerManager, WorkerObserver {
    private String                  _id                          = null;
    /**
     * ProcessManager status
     */
    private WorkerManagerStatus     _status                      = null;
    /**
     * observer
     */
    protected WorkerManagerObserver _listener                    = null;
    /**
     * the service that manages the running threads
     */
    private ExecutorService _es                                  = null;

    /**
     * the execution mode of the {@code AbstractWorkerManager}
     *
     * <li/>{@code SERIAL} - in a given moment there is only one {@link IWorker} running.
     * <li/>{@code NON_SERIAL} - there is no bound on the amount of workers running.
     */
    private EXECUTION_MODE _em                                   = null;
    /**
     * the number of worker active at any moment. only makes sense if it is {@code 1} or {@code Integer.MAX_VALUE},
     * since they are delivered to a {@code Queue} of an {@link ThreadPoolExecutor}. Therefore use {@code 1} if
     * order counts, such as serial execution. that means, a worker has to finish before the next one is delivered
     * for execution.
     */
    protected int _maxWorkersRunningAtOnce                      = Integer.MAX_VALUE;

    /**
     * the execution mode of the {@code AbstractWorkerManager}
     *
     * <li/>{@code SERIAL} - in a given moment there is only one {@link IWorker} running.
     * <li/>{@code NON_SERIAL} - there is no bound on the amount of workers running.
     */
    public enum EXECUTION_MODE {
        SERIAL, NON_SERIAL
    }

    /**
     *
     * @return the {@code EXECUTION_MODE} of the Worker Manager.
     *
     * @see com.hendrix.zorn.managers.AbstractWorkerManager.EXECUTION_MODE
     */
    public EXECUTION_MODE getExecutionMode() {
        return _em;
    }

    /**
     * set the {@code EXECUTION_MODE} of the Worker Manager.
     *
     * <ul>
     *      <li/>{@code SERIAL} - in a given moment there is only one {@link IWorker} running.
     *      <li/>{@code NON_SERIAL} - there is no bound on the amount of workers running.
     * </ul>
     *
     * @param em {@code EXECUTION_MODE.SERIAL, EXECUTION_MODE.NON_SERIAL}
     *
     * @see com.hendrix.zorn.managers.AbstractWorkerManager.EXECUTION_MODE
     */
    public void setExecutionMode(EXECUTION_MODE em) {
        _em = em;

        switch (_em){
            case SERIAL:
                _maxWorkersRunningAtOnce = 1;
                break;
            case NON_SERIAL:
                _maxWorkersRunningAtOnce = Runtime.getRuntime().availableProcessors() + 1;
                break;
        }
    }

    /**
     * set the identifier of the process
     *
     * @param id the identifier
     */
    public AbstractWorkerManager(String id) {
        _id     = id;
        _status = new WorkerManagerStatus();

        setExecutionMode(EXECUTION_MODE.NON_SERIAL);

        setupExecutor();
    }

    public AbstractWorkerManager() {
        this("Anonymous Worker Manager");
    }

    /**
     *	pause the Worker Manager. Currently running workers will not be interrupted
     *  and will complete their work.
     */
    @Override
    public void pause() {
        if(isRunning())
            getStatusInfo().setStatus(WorkerManagerStatus.Status.STATUS_PAUSE);
    }

    /**
     * resume the process manager
     */
    @Override
    public void resume() {
        if(isPaused()) {
            getStatusInfo().setStatus(WorkerManagerStatus.Status.STATUS_IDLE);
            tryRunNextWorker();
        }
    }

    /**
     *
     * stop the worker manager, which includes trying stopping every running {@link IWorker},
     * and clearing the running workers {@link Collection}.
     *
     * @throws UnsupportedOperationException if an {@link IWorker} does not have
     * implementation for {@code AbstractWorker.stop()}
     */
    public void stop()
    {
        if(isReady())
            return;

        getStatusInfo().setStatus(WorkerManagerStatus.Status.STATUS_STOP);

        // here remove all processes

        for (IWorker worker : getRunningWorkers()) {
            worker.stop();
        }

        getRunningWorkers().clear();
        //getPendingWorkers().clear();
    }

    /**
     * enqueue a {@link IWorker} into the {@code AbstractWorkerManager}.
     *
     * @param worker A processable element
     */
    @Override
    public void enqueue(IWorker worker) {
        onEnqueue(worker);

        getStatusInfo().numTotal += 1;

        if(isRunning())
            tryRunNextWorker();
    }

    /**
     * process error callback
     *
     * @param worker the erroneous process
     */
    @Override
    public synchronized  void onWorkerError(IWorker worker)
    {
        pause();

        getRunningWorkers().remove(worker);
        getFailedWorkers().add(worker);

        WorkerManagerErrorInfo pme  = new WorkerManagerErrorInfo(WorkerManagerErrorInfo.ErrorCode.ERROR_FAILED_PROCESS, "Worker with ID: " + worker.getId() + " FAILED!!", worker.getId() );

        getStatusInfo().addError(pme);

        notifyError(pme);
    }

    /**
     * process complete callback
     *
     * @param worker the completed process
     */
    @Override
    public synchronized  void onWorkerComplete(IWorker worker)
    {
        if(storeFinishedWorkers())
            getFinishedWorkers().put(worker.getId(), worker);

        getRunningWorkers().remove(worker);

        getStatusInfo().numComplete += 1;

        // checks if pause or stop were pending
        if(!isRunning())
            return;

        notifyProgress(worker);

        // check completion
        if(sizePendingWorkers()==0 && getRunningWorkers().size()==0) {
            getStatusInfo().setStatus(WorkerManagerStatus.Status.STATUS_IDLE);
            notifyComplete();
            return;
        }

        tryRunNextWorker();
    }

    /**
     * {@link IWorker} progress {@code observer}
     *
     * @param worker the progressed {@link IWorker}
     */
    @Override
    public void onWorkerProgress(IWorker worker) {

    }

    /**
     * start the process manager
     */
    @Override
    public void start()
    {
        if(isRunning()) {
            System.out.println("WorkerManager.start():: is already working: WORKING or IDLE %n");
            return;
        }

        getStatusInfo().setStatus(WorkerManagerStatus.Status.STATUS_IDLE);

        tryRunNextWorker();
    }

    /**
     *
     * retry failed processes.
     * pay attention. default implementation will re enqueue failed workers,
     * therefore, override if necessary.
     */
    public void retry()
    {
        IWorker p;

        Iterator<IWorker> iteratorFailedProcesses = getFailedWorkers().iterator();

        while (iteratorFailedProcesses.hasNext()) {
            p = iteratorFailedProcesses.next();
            iteratorFailedProcesses.remove();

            enqueue(p);
        }

        resume();
    }

    /**
     *
     * @return a printable representation of {@link IWorkerManager}.
     */
    @Override
    public String toString() {
        return "Zorn Worker Manager:: id=" + getId() + ", running#=" + getRunningWorkers().size() + ", finished#=" + getFinishedWorkers().size() + ", failed#=" + getFailedWorkers().size();
    }

    /**
     * set the listener for the worker manager<br/>
     * get notifies of worker completion, progress, error.
     *
     * @param listener the listener
     */
    public void setListener(WorkerManagerObserver listener ) {
        _listener = listener;
    }

    /**
     * @return the identifier of the Manager
     */
    @Override
    public String getId() {
        return _id;
    }

    /**
     *
     * @return flag if process manager status is {@code STATUS_IDLE}
     */
    @Override
    public boolean isIdle()
    {
        return (getStatusInfo().getStatus() == WorkerManagerStatus.Status.STATUS_IDLE);
    }
    /**
     *
     * @return flag if process manager status is {@code STATUS_PAUSE}
     */
    @Override
    public boolean isPaused()
    {
        return (getStatusInfo().getStatus() == WorkerManagerStatus.Status.STATUS_PAUSE);
    }

    /**
     *
     * @return flag if process manager status is {@code STATUS_STOP}
     */
    @Override
    public boolean isStopped()
    {
        return (getStatusInfo().getStatus() == WorkerManagerStatus.Status.STATUS_STOP);
    }

    /**
     *
     * @return flag if process manager status is {@code STATUS_WORKING}
     */
    @Override
    public boolean isWorking()
    {
        return (getStatusInfo().getStatus() == WorkerManagerStatus.Status.STATUS_WORKING);
    }

    /**
     *
     * @return flag if process manager status is {@code (STATUS_WORKING || STATUS_IDLE)}
     */
    @Override
    public boolean isRunning()
    {
        return (isWorking() || isIdle());
    }

    /**
     *
     * @return flag if process manager status is {@code STATUS_READY}
     */
    @Override
    public boolean isReady()
    {
        return (getStatusInfo().getStatus() == WorkerManagerStatus.Status.STATUS_READY);
    }

    /**
     *
     * @return The {@link WorkerManagerStatus} object
     */
    @Override
    public WorkerManagerStatus getStatusInfo() {
        return _status;
    }

    /**
     * dispose the object
     */
    @Override
    public void dispose() {
        getStatusInfo().cleanErrors();

        _status     = null;
        _listener   = null;

        //if(getPendingWorkers()!=null)
       //     getPendingWorkers().clear();

        if(getRunningWorkers()!=null)
            getRunningWorkers().clear();

        if(getFailedWorkers()!=null)
            getFailedWorkers().clear();
    }

    public abstract Collection<IWorker> getFailedWorkers();

    /**
     * would ou like to store finished workers
     *
     * @return {@code true/false}
     */
    protected abstract boolean storeFinishedWorkers();

    /**
     * get the next {@link IWorker} proposed for execution.
     * this method is allowed to return {@code null}.
     * extract the next worker from your unique data structure.
     *
     * @return {@link IWorker}, allowed to return {@code null}.
     */
    protected abstract IWorker getNextWorker();

    /**
     * handle the saving of this {@link IWorker} in your own unique
     * data structures. if you don't have something unique then
     * use:
     * <pre>
     *     getPendingWorkers.add(worker)
     * </pre>
     *
     * @param worker {@link IWorker}
     */
    protected abstract void onEnqueue(IWorker worker);

    /**
     * get currently running workers
     *
     * @return a {@link Collection} of {@link IWorker}
     */
    protected abstract Collection<IWorker> getRunningWorkers();

    //protected abstract Collection<IWorker> getPendingWorkers();

    /**
     * the size of pending workers
     *
     * @return the size
     */
    protected abstract int sizePendingWorkers();

    /**
     * select the next process for work
     */
    protected void tryRunNextWorker()
    {
        IWorker pp;

        if(!canSpawnAnotherProcess())
            return;

        pp 								= getNextWorker();

        if(pp == null)
            return;

        getRunningWorkers().add(pp);

        // could be the case that all processes finished already by the time
        // the first process above finished.
        getStatusInfo().setStatus(WorkerManagerStatus.Status.STATUS_WORKING);

        pp.process(this, _es);

        if(canSpawnAnotherProcess())
            tryRunNextWorker();
    }

    /**
     * can i spawn another worker according to the bound on number of workers delivered to the executor at once?
     *
     * @return true if can spawn, false otherwise
     */
    private boolean canSpawnAnotherProcess()
    {
        if(!isRunning())
            return false;

        int countCurrentRunningProcesses    = getRunningWorkers().size();

        int count                           = Math.min(_maxWorkersRunningAtOnce - countCurrentRunningProcesses, sizePendingWorkers());

        return (count > 0);
    }

    /**
     * setup the thread executor
     */
    private void setupExecutor()
    {
        int count_cpu = Runtime.getRuntime().availableProcessors();

        _es           = new ThreadPoolExecutor(count_cpu + 1, count_cpu*2 + 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), pmThreadFactory);
    }

    /**
     * thread factory handed to the executor
     */
    private final ThreadFactory pmThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @SuppressWarnings("NullableProblems")
        public Thread newThread(Runnable r) {
            return new Thread(r, "Zorn WorkerManager ID: " + getId() + ", worker #" + mCount.getAndIncrement());
        }
    };


}
