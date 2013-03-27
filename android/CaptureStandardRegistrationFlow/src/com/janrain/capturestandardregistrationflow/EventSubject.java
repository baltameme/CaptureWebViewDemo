package com.janrain.capturestandardregistrationflow;

import java.util.Observable;

import org.json.JSONArray;

public class EventSubject extends Observable implements Runnable {
    private JSONArray mJArray;

    public void setEventData(final JSONArray jArray) {
        mJArray = jArray;
    }

    public void run() {
        setChanged();
        notifyObservers(mJArray);
    }
}
