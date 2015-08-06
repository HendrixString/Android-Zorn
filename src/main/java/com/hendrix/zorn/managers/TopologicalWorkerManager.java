package com.hendrix.zorn.managers;

import android.util.Log;

import com.hendrix.graph.algorithms.TopologicalSort;
import com.hendrix.graph.exceptions.NotDirectedAcyclicGraphException;
import com.hendrix.graph.graphs.SimpleDirectedGraph;
import com.hendrix.graph.types.IVertex;
import com.hendrix.graph.types.Vertex;
import com.hendrix.zorn.workers.IWorker;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * a {@link IWorkerManager} implementation based on {@code Topological Sorting}. the class is thread safe.
 *
 * <ul>
 *     <li/>this class cannot be instantiated from everywhere (Protected access level constructor).
 *     <li/>use only {@link com.hendrix.zorn.managers.TopologicalWorkerManager.Builder}.
 * </ul>
 *
 * @see com.hendrix.zorn.managers.TopologicalWorkerManager.Builder
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("unused")
public class TopologicalWorkerManager extends BaseAbstractWorkerManager {
    static public final String ZORN_TOPOLOGICAL_TAG    = "Zorn TopologicalManager";
    static public final String msg_error               = "The workers contain a Cycle, therefore Topological Sorting failed!";

    private LinkedList<IWorker> _pendingWorkers        = null;

    /**
     * for {@code TopologicalWorkerManager}, the only execution mode
     * available is {code EXECUTION_MODE.SERIAL}.
     * this might change in the future when {@code TopologicalSort} will have
     * bundling options for independent sets.
     */
    @Override
    public void setExecutionMode(EXECUTION_MODE em) {
        super.setExecutionMode(EXECUTION_MODE.SERIAL);
    }

    /**
     *
     * @param builder a {@code Builder} instance
     *
     * @see com.hendrix.zorn.managers.TopologicalWorkerManager.Builder
     *
     * @throws Error - if the topological sorting detected a cycle
     */
    protected TopologicalWorkerManager(Builder builder) {
        this(builder._id);

        setExecutionMode(EXECUTION_MODE.SERIAL);
        setListener(builder._listener);

        LinkedList<IVertex> ll_vertex;

        try {
            ll_vertex = new TopologicalSort(builder._sdg).applyAlgorithm();
        } catch (NotDirectedAcyclicGraphException exc) {
            Log.e(ZORN_TOPOLOGICAL_TAG, msg_error);

            throw new Error(ZORN_TOPOLOGICAL_TAG + ":: " + msg_error);
        }

        for (IVertex vertex : ll_vertex) {
            enqueue((IWorker)vertex.getData());
        }

        builder.dispose();
    }

    /**
     * {@inheritDoc}
     *
     * @param id
     */
    protected TopologicalWorkerManager(String id) {
        super(id);

        _pendingWorkers = new LinkedList<>();
    }

    /**
     * {@inheritDoc}
     */
    protected TopologicalWorkerManager() {
        this("");
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
        return _pendingWorkers.pollFirst();
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

    @SuppressWarnings("unused")
    public static class Builder {
        private SimpleDirectedGraph         _sdg        = null;
        private String                      _id         = null;
        private WorkerManagerObserver       _listener   = null;

        private HashMap<IWorker, Vertex<IWorker>> _mapWorkerVertex = null;

        public Builder() {
            _sdg                = new SimpleDirectedGraph();
            _mapWorkerVertex    = new HashMap<>();
        }

        /**
         * build the {@link TopologicalWorkerManager}
         *
         * @return a {@link TopologicalWorkerManager} instance
         */
        public TopologicalWorkerManager build()
        {
            return new TopologicalWorkerManager(this);
        }

        protected void dispose() {
            _sdg.dispose();

            _mapWorkerVertex.clear();

            _id                 = null;
            _sdg                = null;
            _listener           = null;
            _mapWorkerVertex    = null;
        }

        /**
         * put a {@link IWorker} before another {@link IWorker}
         *
         * @param put    the {@link IWorker} to put before the other {@code IWorker}
         * @param before the other {@code IWorker}
         *
         * @return {@link com.hendrix.zorn.managers.TopologicalWorkerManager.Builder}
         */
        public Builder before(IWorker put, IWorker before) {
            Vertex<IWorker> vertex_put      = vertexOf(put);
            Vertex<IWorker> vertex_before   = vertexOf(before);

            _sdg.addVertex(vertex_put);
            _sdg.addVertex(vertex_before);

            _sdg.addEdge(vertex_put, vertex_before);

            return this;
        }

        /**
         * put a {@link IWorker} after another {@link IWorker}
         *
         * @param put    the {@link IWorker} to put after the other {@code IWorker}
         * @param after  the other {@code IWorker}
         *
         * @return {@link com.hendrix.zorn.managers.TopologicalWorkerManager.Builder}
         */
        public Builder after(IWorker put, IWorker after) {
            Vertex<IWorker> vertex_put      = vertexOf(put);
            Vertex<IWorker> vertex_after    = vertexOf(after);

            _sdg.addVertex(vertex_put);
            _sdg.addVertex(vertex_after);

            _sdg.addEdge(vertex_after, vertex_put);

            return this;
        }

        /**
         * set the listener for the {@link TopologicalWorkerManager}
         *
         * @param listener a {@link WorkerManagerObserver} instance
         *
         * @return {@link com.hendrix.zorn.managers.TopologicalWorkerManager.Builder}
         */
        public Builder listener(WorkerManagerObserver listener) {
            _listener = listener;

            return this;
        }

        /**
         * set the {@code identifier} for the {@code TopologicalWorkerManager}
         *
         * @param id {@code identifier}
         *
         * @return {@link com.hendrix.zorn.managers.TopologicalWorkerManager.Builder}
         */
        public Builder id(String id) {
            _id = id;

            return this;
        }

        /**
         * we have to store the mapping of worker to vertices.
         * also, in case of new unseen workers(hashcode wise),
         * map them.
         *
         * @param worker the {@code IWorker}
         *
         * @return the corresponding {@code Vertex}
         */
        private Vertex<IWorker> vertexOf(IWorker worker) {
            Vertex<IWorker> vertex = _mapWorkerVertex.get(worker);

            if(vertex != null)
                return vertex;

            vertex       = new Vertex<>();

            vertex.setData(worker);

            _mapWorkerVertex.put(worker, vertex);

            return vertex;
        }

    }

}
