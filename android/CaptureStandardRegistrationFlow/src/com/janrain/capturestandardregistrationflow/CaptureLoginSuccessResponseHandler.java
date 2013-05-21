/**
 * 
 */
package com.janrain.capturestandardregistrationflow;

import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author mitch
 * 
 * Observer of Capture login success. Responds by parsing data returned from 
 * onCaptureLoginSuccess. 
 * 
 * @see http://en.wikipedia.org/wiki/Observer_pattern
 */
public class CaptureLoginSuccessResponseHandler implements Observer {

    private String mAccessToken;

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
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

    /**
     * @return the mAccessToken
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * @param mAccessToken
     *            the mAccessToken to set
     */
    public void setAccessToken(String mAccessToken) {
        this.mAccessToken = mAccessToken;
    }
}
