package com.hendrix.zorn.managers;

import com.hendrix.zorn.interfaces.IDisposable;

import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class WorkerManagerStatus implements IDisposable
{

    public enum Status {
        /**
         * {@code STATUS_READY} - represents a process manager that is not working, but is ready to start for first time.
         */
        STATUS_READY,

        /**
         * {@code STATUS_WORKING} - represents a process manager that is currently working one process or more.
         */
        STATUS_WORKING,

        /**
         * {@code STATUS_IDLE} - represents a process manager that is active but currently is not working any process.
         */
        STATUS_IDLE,

        /**
         * {@code STATUS_PAUSE} - represents a process manager that is currently pausing or has paused.
         */
        STATUS_PAUSE,

        /**
         * {@code STATUS_STOP} - represents a process manager that has stopped and removed all of it's processes.
         */
        STATUS_STOP
    }

    public 	boolean 												flagTraceLog		=	true;

    public 	int 														numComplete			= 0;
    public 	int 														numTotal				= 0;

    private Status 													_status					= null;

    private ArrayList<WorkerManagerErrorInfo> 	_errors					=	null;

    public WorkerManagerStatus()
    {
        _errors	=	new ArrayList<>();

        _status	=	Status.STATUS_READY;
    }

    /**
     * @param error an error
     */
    public void addError(WorkerManagerErrorInfo error)
    {
        _errors.add(error);

        if(flagTraceLog)
            System.out.printf("ProcessManager Error: code=%s, msg=%s, dataAux=%s %n", error.getCodeError(), error.getMsgError(), error.getDataAux().toString());
    }

    /**
     * errors description
     */
    public ArrayList<WorkerManagerErrorInfo> getErrors()
    {
        return _errors;
    }

    /**
     * clean errors
     */
    public void cleanErrors()
    {
        _errors.clear();
    }

    /**
     * @inheritDoc
     */
    public void dispose()
    {
        if(flagTraceLog)
            System.out.printf("ProcessManager Dispose() %n");

        cleanErrors();

        _errors	=	null;
    }

    /**
     * get the status of the worker manager
     *
     * @return the {@link WorkerManagerStatus.Status}
     */
    public Status getStatus()	{	return _status;	}

    /**
     * set a new {@link WorkerManagerStatus.Status}
     *
     * @param value the status
     */
    public void setStatus(Status value)
    {
        _status = value;

        if(flagTraceLog)
            System.out.printf("ProcessManager new status: %s %n", _status);
    }

}