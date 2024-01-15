package com.thecn.app.tools;

import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;

/**
 * Credit to quickdraw mcgraw for original idea to extend a handler for handling messages only when
 * an activity/fragment is resumed
 * http://stackoverflow.com/a/8122789
 *
 * Using the idea as a springboard, this class processes Runnables instead of Messages
 */
public class PausingHandler extends Handler {

    //buffer for actions added while the handler was paused
    final LinkedList<Runnable> mRunnableQueue = new LinkedList<Runnable>();

    private boolean paused;

    /**
     * Resumes handler and runs any Runnables that have accumulated in the queue
     */
    final public void resume() {
        paused = false;

        boolean notNull;
        do {
            Runnable action = mRunnableQueue.poll();
            notNull = action != null;
            if (notNull) {
                post(action);
            }
        } while (notNull);
    }

    /**
     * Pause the handler
     */
    final public void pause() {
        paused = true;
    }

    /**
     * Posts a response if the handler is not paused,
     * otherwise adds the action to the queue
     * @param action an action to perform
     * @return false if the action could not be posted immediately
     */
    public boolean postWhenResumed(Runnable action) {
        if (paused) {
            mRunnableQueue.offer(action);
            return false;
        }

        return post(action);
    }

    public void clearQueue() {
        mRunnableQueue.clear();
    }
}