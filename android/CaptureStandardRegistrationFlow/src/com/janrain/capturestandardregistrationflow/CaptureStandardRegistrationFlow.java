package com.janrain.capturestandardregistrationflow;

import android.app.Application;

public class CaptureStandardRegistrationFlow extends Application {

    private CaptureLoginSuccessEventSubject mCaptureLoginSuccessEventSubject = null;

    public CaptureStandardRegistrationFlow() {
        super();
        mCaptureLoginSuccessEventSubject = new CaptureLoginSuccessEventSubject();
    }

    /**
     * @return the eventSubject
     */
    public CaptureLoginSuccessEventSubject getCaptureLoginSuccessEventSubject() {
        return mCaptureLoginSuccessEventSubject;
    }
}
