package com.janrain.capturestandardregistrationflow;

import java.util.Observable;

import org.json.JSONArray;

/**
 * @author mitch
 * 
 * Subject of Capture login success. Notifies observers by setting the event's JSONArray. 
 * 
 * @see http://en.wikipedia.org/wiki/Observer_pattern
 */

public class CaptureLoginSuccessEventSubject extends Observable implements Runnable {
    private JSONArray mJsonArray;

    public void setEventData(final JSONArray jsonArray) {
        mJsonArray = jsonArray;
    }

    public void run() {
        setChanged();
        notifyObservers(mJsonArray);
    }
}
