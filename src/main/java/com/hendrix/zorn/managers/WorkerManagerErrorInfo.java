package com.hendrix.zorn.managers;

/**
 * worker manager errors
 *
 * @author Tomer Shalev
 */
@SuppressWarnings("UnusedDeclaration")
public class WorkerManagerErrorInfo
{

    public enum ErrorCode {
        /**
         * <code>ERROR_FAILED_PROCESS</code> - represents a process that has failed.
         */
        ERROR_FAILED_PROCESS,

        /**
         * <code>NO_ERROR</code> - NO ERROR.
         */
        NO_ERROR
    }

    private String      _msgError                     = ErrorCode.NO_ERROR.toString();
    private ErrorCode   _codeError                    = ErrorCode.NO_ERROR;

    private Object      _dataAux                      = null;

    public WorkerManagerErrorInfo(ErrorCode codeError, String msg, Object dataAux)
    {
        _msgError   = msg;
        _codeError  = codeError;
        _dataAux    = dataAux;
    }

    public WorkerManagerErrorInfo()
    {
    }

    public void setError(ErrorCode codeError, String msg, Object dataAux)
    {
        _msgError   = msg;
        _codeError  = codeError;
        _dataAux    = dataAux;
    }

    /**
     * error description.
     */
    public String getMsgError() {
        return _msgError;
    }
    public void setMsgError(String value) {
        _msgError = value;
    }

    /**
     * error code.
     */
    public ErrorCode getCodeError() {
        return _codeError;
    }
    public void setCodeError(ErrorCode value) {
        _codeError = value;
    }

    /**
     * auxiliary data to pass along with the error, can be id or a process or whatever.
     */
    public Object getDataAux() {
        return _dataAux;
    }
    public void setDataAux(Object value) {
        _dataAux = value;
    }

}
