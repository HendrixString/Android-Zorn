package com.hendrix.zorn.managers;

import com.hendrix.zorn.interfaces.IPriority;
import com.hendrix.zorn.workers.IWorker;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * a {@link IWorkerManager} implementation based on priorities. the class is thread safe.
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("UnusedDeclaration")
public class PriorityWorkerManager extends BaseAbstractWorkerManager {
    /**
     * the queue that holds pending processes
     */
    private Queue<IWorker> _pendingWorkers = null;

    /**
     * {@inheritDoc}
     *
     * @param id
     */
    public PriorityWorkerManager(String id) {
        super(id);

        Comparator<IPriority> comparator 	= new PriorityComparator();

        _pendingWorkers                     = new PriorityQueue<>(10, comparator);
    }

    /**
     * {@inheritDoc}
     */
    public PriorityWorkerManager() {
        this("Anonymous Priority Manager");
    }

    /**
     * stop the worker manager
     */
    @Override
    public void stop() {
        super.stop();

        _pendingWorkers.clear();
    }

    /**
     * get the next {@link IWorker} proposed for execution.
     * this method is allowed to return {@code null}.
     * extract the next worker from your unique data structure.
     *
     * @return {@link IWorker}, allowed to return {@code null}.
     */
    @Override
    protected IWorker getNextWorker() {
        return _pendingWorkers.poll();
    }

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
    @Override
    protected void onEnqueue(IWorker worker) {
        _pendingWorkers.add(worker);
    }

    /**
     * the size of pending workers
     *
     * @return the size
     */
    @Override
    protected int sizePendingWorkers() {
        return _pendingWorkers.size();
    }

    /**
     * dispose the object
     */
    @Override
    public void dispose() {
        super.dispose();

        _pendingWorkers.clear();
        _pendingWorkers = null;
    }

    /**
     * a  custom {@link Comparator} used for priority resolution.
     * used for the {@code withPriority} mode.
     */
    private static class PriorityComparator implements Comparator<IPriority>
    {
        @Override
        public int compare(IPriority x, IPriority y)
        {
            if (x.getPriority() < y.getPriority())
            {
                return 1;
            }
            if (x.getPriority() > y.getPriority())
            {
                return -1;
            }
            return 0;
        }

    }

}
