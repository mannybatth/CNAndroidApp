package com.thecn.app.stores;

import com.android.volley.VolleyError;
import com.thecn.app.tools.PausingHandler;

import org.json.JSONObject;

/**
* Created by philjay on 7/8/14.
*/
public abstract class ResponseCallback {

    public static final int HANDLE_NONE = 0;
    public static final int HANDLE_SUCCESS = 1;
    public static final int HANDLE_FAILURE = 2;
    public static final int HANDLE_ERROR = 4;
    public static final int HANDLE_ALL = 7;

    /**
     * If a flag bit (see above) is set, then the
     * corresponding method (see below) should be handled
     * by the PausingHandler as opposed to being executed
     * immediately on network response.
     */
    private int mFlags;

    public int getFlags() {
        return mFlags;
    }

    public void setFlags(int flags) {
        if (mHandler == null) {
            mFlags = HANDLE_NONE;
        } else {
            mFlags = flags;
        }
    }

    public enum ExecutionType {
        SUCCESS, FAILURE, ERROR
    }

    /**
     * Set to indicate which type of execution will
     * be run for this instance of ResponseCallback
     *
     * onSuccess, onFailure, or onError
     */
    private ExecutionType mExecutionType;

    public void setExecutionType (ExecutionType executionType) {
        mExecutionType = executionType;
    }

    public ExecutionType getExecutionType() {
        return mExecutionType;
    }

    /**
     * This is intended to be used to make updates to UI
     * components.  The reason for its use is because it's
     * possible that a network call could return during an
     * orientation change, where a fragment has had its view
     * destroyed or an activity's instance is or is being destroyed.
     * If properly used, the handler will wait until the activity
     * or fragment has been resumed before performing the callbacks.
     */
    PausingHandler mHandler;

    //will not use a handler
    public ResponseCallback() {
        mFlags = HANDLE_NONE;
    }

    //will use a handler, unless parameter is null
    public ResponseCallback(PausingHandler handler) {
        setHandler(handler);
    }

    public PausingHandler getHandler() {
        return mHandler;
    }

    public void setHandler(PausingHandler handler) {
        setHandler(handler, HANDLE_ALL);
    }

    public void setHandler(PausingHandler handler, int flags) {
        mHandler = handler;

        setFlags(flags);
    }

    /**
     * Callback methods to be implemented as needed.
     * The order of the methods should be:
     *
     * For response received:
     *
     * onImmediateResponse
     *
     * onPreExecute
     * if (success) onSuccess
     * else onFailure
     * onPostExecute
     *
     * executeWithHandler
     *
     * OR
     *
     * For network errors:
     *
     * onImmediateResponse
     *
     * onPreExecute
     * onError
     * onPostExecute
     *
     * executeWithHandler
     *
     * see BaseStore for implementation
     */

    //should be called right when response is received (never called by handler)
    public void onImmediateResponse() {}

    //always called directly before main execution method
    public void onPreExecute() {}

    //the main execution method is one of these three, depending on the response
    public void onSuccess(final JSONObject response) {}

    public void onFailure(final JSONObject response) {}

    public void onError(final Exception error) {}

    //always called directly after the main execution method
    public void onPostExecute() {}

    /**
     * This method will always be called by the handler (if not null)
     * For example, this can be used if an update to the UI should occur
     * if the UI component is still available
     */
    public void executeWithHandler(final JSONObject response) {}

    public void executeWithHandler(final Exception error) {}
}
