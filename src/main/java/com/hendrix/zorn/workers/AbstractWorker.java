package com.hendrix.zorn.workers;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hendrix.zorn.Zorn;
import com.hendrix.zorn.managers.IWorkerManager;

import java.util.concurrent.ExecutorService;

/**
 * Abstract {@link IWorker} base pre implementation.
 * <p/>
 * <b>Quick Outline:</b>
 * <ul>
 *     <li>Implement {@link #work()} method to define a synchronous work block or also start an async code through it.
 *     <li>Use {@link #process(WorkerObserver)} to execute the {@link IWorker} on a different thread.
 *     <li>Use {@link #work()} to execute the {@link IWorker} on the thread that called this method.
 *     <li>Use {@code setAutomaticCompleteNotify(false)} and {@link #notifyComplete()} to notify completion on
 *     the thread that started this worker, if this this worker itself contains async code execution within it's {@link #work()} method.
 *     <li>By default {@code setAutomaticCompleteNotify(true)} and {@link #work()} method is presumed by default to contain synchronous
 *     code (like {@link android.os.AsyncTask} and Java's Runnable when used with threads or executors)
 *     <li>therefore {@link AbstractWorker} gives you more diverse possibilities concurrency wise when you consider {@link android.os.AsyncTask}
 * </ul>
 *
 * the idea behind a {@link IWorker} is that it's {@code work()} method
 * may contain a synchronous or async work to be done, in both cases the worker's {@code work()} method will run
 * on a different a thread if you use the {@code process()} method, or on the calling thread using {@code work()} method.
 * in the case that synchronous code is used inside {@code work()} method, set:
 * <pre>
 *     setAutomaticCompleteNotify(true);
 * </pre>
 * so that the worker will notify to it's parent thread, that it's work is done.
 * otherwise, if the code inside {@code work()} method may itself start another async code relevant
 * to the worker's work, then set:
 * <pre>
 *     setAutomaticCompleteNotify(false);
 * </pre>
 * and when the work is done, simply use
 * <pre>
 *     notifyComplete(); or notifyProgress();
 * </pre>
 *
 * a {@link IWorker} that runs on a different a thread, always returns results <b>{progress, complete, error}</b>, on the
 * thread that started the worker, so you don't have to worry about <b>concurrency</b> at all
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("UnusedDeclaration")
abstract public class AbstractWorker implements IWorker
{
    static final String ZORN_WORKER_TAG = "Zorn Worker";

    /**
     * internal messages codes for completion, error, progress.
     */
    private static final int MESSAGE_COMPLETE     = 0x1;
    private static final int MESSAGE_ERROR        = 0x2;
    private static final int MESSAGE_PROGRESS     = 0x3;

    /**
     * worker priority
     */
    protected int 				_priorityKey		=	0;
    /**
     * worker identification
     */
    protected String 			_id				    = null;

    /**
     * worker callbacks
     */
    private WorkerObserver      _observer           = null;
    /**
     * internal {@link Runnable} for {@link ExecutorService}.
     */
    private Runnable          _runner               = null;
    /**
     * internal {@link Handler} for moving results from background thread into calling thread.
     */
    private Handler           _handler              = null;

    /**
     * notify completion automatically after the worker has finished
     */
    private boolean _flagAutomaticCompleteNotify    = true;

    volatile private Status   _status             = Status.STATUS_READY;

    public enum Status {
        /**
         * {@code STATUS_READY} - represents an {@link AbstractWorker} that is not working, but is ready to start for first time.
         */
        STATUS_READY,

        /**
         * {@code STATUS_WORKING} - represents an {@link AbstractWorker} that is currently working.
         */
        STATUS_WORKING,

        /**
         * {@code STATUS_IDLE} - represents an {@link AbstractWorker} that has completed it's work.
         */
        STATUS_COMPLETE,
        /**
         * {@code STATUS_PAUSE} - represents an {@link AbstractWorker} that is currently pausing or has paused.
         */
        STATUS_PAUSE,
        /**
         * {@code STATUS_ERROR} - represents an {@link AbstractWorker} that has stopped due to ERROR.
         */
        STATUS_ERROR,
        /**
         * {@code STATUS_STOP} - represents an {@link AbstractWorker} that has stopped and removed all of it's processes.
         */
        STATUS_STOP
    }

    /**
     * constructor with parameters that are essential when the {@link IWorker}
     * is running inside a {@link IWorkerManager}
     *
     * @param id            identifier of worker (Optional).
     * @param priorityKey   the priority of the worker (Optional).
     */
    public AbstractWorker(String id, int priorityKey)
    {
        _id             = id;
        _priorityKey    = priorityKey;

        if(_id == null)
            _id         = String.valueOf(System.currentTimeMillis());

        internal_init();
    }

    /**
     * constructor with parameters that are essential when the {@link IWorker}
     * is running inside a {@link IWorkerManager}
     *
     * @param id            identifier of worker (Optional).
     */
    public AbstractWorker(String id) {
        this(id, 0);
    }

    /**
     * empty constructor.
     */
    public AbstractWorker() {
        this("Anonymous Worker", 0);
    }

    /**
     * process the item
     *
     */
    @Override
    final public void process()
    {
        process(null, null);
    }

    /**
     * process the item
     *
     * @param workerObserver callback interface for a process
     */
    @Override
    final public void process(WorkerObserver workerObserver)
    {
        process(workerObserver, null);
    }

    /**
     * process the item with multithreaded capabilities
     *
     * @param workerObserver  callback interface for a process
     * @param es              the {@code ExecutorService} to interact with
     */
    @Override
    final public void process(WorkerObserver workerObserver, ExecutorService es)
    {
        _observer = workerObserver;

        if(es != null)
            es.execute(_runner);
        else {
            Zorn.defaultExecutorService.execute(_runner);
           // _runner.run();
        }
    }

    /**
     * stop processing the item
     */
    public void stop() {
        throw new UnsupportedOperationException("stop() is not implemented in your worker!!");
    }

    /**
     *
     * @return a printable representation of an {@link IWorker}.
     */
    @Override
    public String toString() {
        return "Zorn Worker:: id=" + getId() + ", priority=" + getPriority() + ", status=" + _status.name() + ", thread:" + Thread.currentThread().getName();
    }

    /**
     * notify completion
     */
    @Override
    final public void notifyComplete()
    {
        Message msg = _handler.obtainMessage(MESSAGE_COMPLETE);

        msg.sendToTarget();
    }

    /**
     * notify Error
     */
    @Override
    final public void notifyError()
    {
        Message msg = _handler.obtainMessage(MESSAGE_ERROR);

        msg.sendToTarget();
    }

    /**
     * notify Progress
     */
    @Override
    final public void notifyProgress()
    {
        Message msg = _handler.obtainMessage(MESSAGE_PROGRESS);

        msg.sendToTarget();
    }

    /**
     * dispose the worker
     */
    @Override
    public void dispose()
    {
        _observer = null;
        _handler  = null;
        _runner   = null;
    }

    /**
     * set identifier for the worker
     *
     * @param id the identifier
     */
    @Override
    public void setId(String id)
    {
        _id = id;
    }
    /**
     * get identifier of the worker
     */
    @Override
    public String getId()
    {
        return _id;
    }

    /**
     * set priority for the worker
     *
     * @param key the priority
     */
    @Override
    public void setPriority(int key)
    {
        _priorityKey = key;
    }
    /**
     * get priority of the worker
     *
     */
    @Override
    public int getPriority()
    {
        return _priorityKey;
    }

    /**
     * notify completion automatically after the worker has finished it's {@code work()} method.
     */
    public boolean isAutomaticCompleteNotify()
    {
        return _flagAutomaticCompleteNotify;
    }

    /**
     * set notify completion automatically after the worker has finished
     *
     * @param flag {@code true/false}
     */
    public void setAutomaticCompleteNotify(boolean flag)
    {
        _flagAutomaticCompleteNotify = flag;
    }

    /**
     * is the worker ready (has not run yet)?
     *
     * @return {@code true/false}
     */
    @Override
    public boolean isReady() {
        return _status==Status.STATUS_READY;
    }

    /**
     * is the worker running?
     *
     * @return {@code true/false}
     */
    @Override
    public boolean isWorking() {
        return _status==Status.STATUS_WORKING;
    }

    /**
     * has the worker finished?
     *
     * @return {@code true/false}
     */
    @Override
    public boolean isFinished() {
        return _status==Status.STATUS_COMPLETE;
    }

    /**
     * called when work has progressed, if you do not prefer to use {@link com.hendrix.zorn.workers.WorkerObserver}
     */
    abstract protected void onProgress();

    /**
     * called when work is complete, if you do not prefer to use {@link com.hendrix.zorn.workers.WorkerObserver}
     */
    abstract protected void onComplete();

    /**
     * override this for error management
     */
    protected void onError() {
        Log.e(ZORN_WORKER_TAG, "Error!!");
    };

    /**
     * internal init
     */
    private void internal_init()
    {
        _handler  = new WorkerHandler();

        _runner   = new Runnable() {
            @Override
            public void run() {
                _status = Status.STATUS_WORKING;

                work();

                if(_flagAutomaticCompleteNotify)
                    notifyComplete();
            }
        };

    }

    /**
     * notifies completion to the listener
     */
    private void internal_notifyComplete()
    {
        _status = Status.STATUS_COMPLETE;

        onComplete();

        if(_observer != null)
            _observer.onWorkerComplete(this);
    }
    /**
     * notifies error to the listener
     */
    private void internal_notifyError()
    {
        _status = Status.STATUS_ERROR;

        if(_observer != null)
            _observer.onWorkerError(this);
    }
    /**
     * notifies error to the listener
     */
    private void internal_notifyProgress()
    {
        onProgress();

        if(_observer != null)
            _observer.onWorkerProgress(this);
    }

    @SuppressLint("HandlerLeak")
    /**
     * the custom {@link Handler} of the worker
     */
    private class WorkerHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            int code_res = msg.what;

            switch (code_res) {
                case MESSAGE_COMPLETE:
                    internal_notifyComplete();
                    break;
                case MESSAGE_ERROR:
                    internal_notifyError();
                    break;
                case MESSAGE_PROGRESS:
                    internal_notifyProgress();
                    break;
            }

        }

    }

}