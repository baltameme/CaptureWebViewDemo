package com.janrain.capturestandardregistrationflow;

import android.app.Application;

public class CaptureStandardRegistrationFlow extends Application {

    private EventSubject mEventSubject = null;

    public CaptureStandardRegistrationFlow() {
        super();
        mEventSubject = new EventSubject();
    }

    /**
     * @return the eventSubject
     */
    public EventSubject getEventSubject() {
        return mEventSubject;
    }
}
