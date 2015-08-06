package com.hendrix.zorn;

import com.hendrix.zorn.managers.PriorityWorkerManager;
import com.hendrix.zorn.managers.TopologicalWorkerManager;

/**
 * Use this {@code factory} class to find out about the various {@link com.hendrix.zorn.managers.IWorkerManager} implementations.
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("unused")
public class Zorn {

    private Zorn() {
    }

    /**
     * get a {@code PriorityWorkerManager}.
     *
     * @return a {@link PriorityWorkerManager} instance
     */
    static public PriorityWorkerManager newPriorityWorkerManager() {
        return new PriorityWorkerManager();
    }

    /**
     * get a {@code TopologicalWorkerManager.Builder}.
     *
     * @return a {@link TopologicalWorkerManager.Builder} instance.
     *
     * @see com.hendrix.zorn.managers.TopologicalWorkerManager.Builder
     */
    static public TopologicalWorkerManager.Builder newTopologicalWorkerManager() {
        return new TopologicalWorkerManager.Builder();
    }

}
