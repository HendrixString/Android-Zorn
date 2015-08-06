package com.hendrix.zorn.workers;

import com.hendrix.zorn.interfaces.IDisposable;
import com.hendrix.zorn.interfaces.IId;
import com.hendrix.zorn.interfaces.IPriority;
import com.hendrix.zorn.interfaces.IWorkable;

/**
 * interface for worker
 *
 * @author Tomer Shalev
 *
 */
@SuppressWarnings("UnusedDeclaration")
public interface IWorker extends IId, IPriority, IWorkable, IDisposable {
    /**
     * is the worker running?
     *
     * @return {@code true/false}
     */
    boolean isWorking();

    /**
     * has the worker finished?
     *
     * @return {@code true/false}
     */
    boolean isFinished();

    /**
     * is the worker ready (has not run yet)?
     *
     * @return {@code true/false}
     */
    boolean isReady();

}
