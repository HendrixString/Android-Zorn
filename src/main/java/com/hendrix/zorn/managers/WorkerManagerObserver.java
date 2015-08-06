package com.hendrix.zorn.managers;

public interface WorkerManagerObserver {
    /**
     * @param wm the process manager instance
     */
    void onComplete(IWorkerManager wm);

    /**
     * @param id the identifier of latest finished process
     */
    void onProgress(String id);

    /**
     * @param err error message
     */
    void onError(WorkerManagerErrorInfo err);

}
