/*
 *
 *  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  *  Copyright (c) 2013, Janrain, Inc.
 *  *
 *  *  All rights reserved.
 *  *
 *  *  Redistribution and use in source and binary forms, with or without modification,
 *  *  are permitted provided that the following conditions are met:
 *  *
 *  *  * Redistributions of source code must retain the above copyright notice, this
 *  *    list of conditions and the following disclaimer.
 *  *  * Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation and/or
 *  *    other materials provided with the distribution.
 *  *  * Neither the name of the Janrain, Inc. nor the names of its
 *  *    contributors may be used to endorse or promote products derived from this
 *  *    software without specific prior written permission.
 *  *
 *  *
 *  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 */
package com.janrain.example.capturestandardregistrationflow;

import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author mitch
 * 
 * Observer of Capture login success. Responds by parsing data returned from onCaptureLoginSuccess.
 * 
 * http://en.wikipedia.org/wiki/Observer_pattern
 */
public class CaptureLoginSuccessResponseHandler implements Observer {
    private String mAccessToken;

    public void update(Observable obj, Object arg) {
        if (arg instanceof JSONArray) {
            final JSONArray jsonArray = (JSONArray) arg;
            if (jsonArray != null) {
                final int length = jsonArray.length();
                for (int i = 0; i < length; ++i) {
                    try {
                        final JSONObject jsonObject = jsonArray
                                .getJSONObject(i);
                        // final String screen = jsonObject.getString("screen");
                        // final String transactionId =
                        // jsonObject.getString("transactionId");
                        // final boolean oneTime =
                        // jsonObject.getBoolean("oneTime");
                        final String accessToken = jsonObject
                                .getString("accessToken");
                        // final String status = jsonObject.getString("status");
                        // final boolean keepMeLoggedIn =
                        // jsonObject.getBoolean("keepMeLoggedIn");
                        // final String flow = jsonObject.getString("flow");

                        // final JSONObject userData =
                        // jsonObject.getJSONObject("userData");
                        // final String displayName =
                        // userData.getString("displayName");
                        // final String email = userData.getString("email");
                        // final String uuid = userData.getString("uuid");

                        // final boolean renders =
                        // jsonObject.getBoolean("renders");
                        // final String version =
                        // jsonObject.getString("version");
                        // final String statusMessage =
                        // jsonObject.getString("statusMessage");
                        // final String action = jsonObject.getString("action");
                        // final boolean ssoImplicitLogin =
                        // jsonObject.getBoolean("ssoImplicitLogin");
                        // final JSONObject authProfileData =
                        // jsonObject.getJSONObject("authProfileData");
                        // final String authProvider =
                        // jsonObject.getString("authProvider");

                        setAccessToken(accessToken);
                    } catch (JSONException e) {
                    }
                }
            }
        }
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String mAccessToken) {
        this.mAccessToken = mAccessToken;
    }
}
