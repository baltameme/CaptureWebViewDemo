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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SignInActivity extends Activity {
    private static final String ANDROID_NS = "demo";
    private static final String SCHEME_JANRAIN = "janrain";

    private WebView mWebView;
    private Handler mHandler = new Handler();

    private static boolean isSchemeJanrain(final String url) {
        boolean result = false;
        if (!TextUtils.isEmpty(url)) {
            final Uri uri = Uri.parse(url);
            final String scheme = uri.getScheme();
            if (!TextUtils.isEmpty(scheme)) {
                if (scheme.equalsIgnoreCase(SCHEME_JANRAIN)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * WebAppInterface class is used to Java <-> JavaScript binding for the WebView
     * For more details, @see http://developer.android.com/guide/webapps/webview.html#BindingJavaScript
     */
    public class WebAppInterface {
        private static final String SIGNIN_COMPLETE_TAG = "signin complete";
        private static final String LOG_TAG = "WebAppInterface";

        final static String JAVASCRIPT = "javascript:"
                + "(function() {"
                + "  if (typeof createJanrainBridge.eventQueue === 'undefined') return \"undefined queue\";"
                + "  var t = JSON.stringify(createJanrainBridge.eventQueue);"
                + "  window." + ANDROID_NS + ".bridgeCallback(t);"
                + "})();";

        private Context mContext = null;

        /** Instantiate the interface and set the context */
        public WebAppInterface(Context context) {
            mContext = context;
        }

        public void runJavascript() {
            mHandler.post(new Runnable() {
                public void run() {
                    mWebView.loadUrl(JAVASCRIPT);
                }
            });
        }

        @JavascriptInterface
        public void bridgeCallback(final String eventQueueString) {
            dispatchEventQueue(eventQueueString);
        }

        private void showSignInCompleteDialogFragment(final JSONArray jsonArray) {
            String jsonArrayString = jsonArray.toString();
            try {
                jsonArrayString = jsonArray.toString(4);
            } catch (JSONException e) {
            }

            AlertDialog.Builder adb = new AlertDialog.Builder(SignInActivity.this);
            adb.setTitle("Sign-In Complete");
            adb.setMessage(jsonArrayString);
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            adb.show();
        }

        private void parseAndDispatchEventUrl(final String argsUrl) {
            // argsUrl will look like:
            // janrain:eventNameHere?arguments=URL%20ENCODED%20JSON%20ARRAY%20HERE

            if (isSchemeJanrain(argsUrl)) {
                String eventName = null;

                final int schemeJanrainLength = SCHEME_JANRAIN.length();
                final char colon = argsUrl.charAt(schemeJanrainLength);
                if (colon == ':') {
                    final int start = schemeJanrainLength + 1;
                    final int indexQuestionMark = argsUrl.indexOf('?', start);
                    if (indexQuestionMark > start) {
                        eventName = argsUrl.substring(start, indexQuestionMark);
                    }
                }

                if (TextUtils.isEmpty(eventName)) {
                    Log.w(LOG_TAG, "Empty Event");
                } else {
                    final String ON_CAPTURE_LOGIN_SUCCESS = "onCaptureLoginSuccess";

                    if (eventName.equals(ON_CAPTURE_LOGIN_SUCCESS)) {
                        String jsonValue = null;

                        final String uriString = Uri.decode(argsUrl);

                        if (uriString != null) {
                            final String ARGUMENTS = "arguments" + "=";

                            final int index = uriString.indexOf(ARGUMENTS);
                            if (index >= 0) {
                                final int start = index + ARGUMENTS.length();
                                jsonValue = uriString.substring(start);
                            }
                        }
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = new JSONArray(jsonValue);
                        } catch (JSONException e) {
                        }
                        if (jsonArray != null) {
                            showSignInCompleteDialogFragment(jsonArray);
                            CaptureStandardRegistrationFlowDemo appState =
                                    (CaptureStandardRegistrationFlowDemo) getApplication();
                            final CaptureLoginSuccessEventSubject captureLoginSuccessEventSubject = 
                                    appState.getCaptureLoginSuccessEventSubject();
                            captureLoginSuccessEventSubject.setEventData(jsonArray);
                            final Thread t = new Thread(captureLoginSuccessEventSubject);
                            t.start();
                        } else {
                            Log.e(LOG_TAG, "Invalid JSON Array format");
                        }
                    } else {
                        Log.i(LOG_TAG, "Ignored Event: " + eventName);
                    }
                }
            } else {
                Log.w(LOG_TAG, "Unknown Scheme");
            }
        }

        private void dispatchEventQueue(final String eventQueueString) {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(eventQueueString);
            } catch (JSONException e) {
            }
            if (jsonArray == null) {
                Log.e(LOG_TAG, "Empty event queue");
            } else {
                final int length = jsonArray.length();
                for (int i = 0; i < length; ++i) {
                    String argsUrl = null;
                    try {
                        argsUrl = jsonArray.getString(i);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Invalid Event");
                    }
                    if (argsUrl != null) {
                        parseAndDispatchEventUrl(argsUrl);
                    }
                }
            }
        }
    }

    public class MyWebViewClient extends WebViewClient {
        private WebAppInterface mWebAppJavascriptBridgeInterface;

        public MyWebViewClient(WebAppInterface webAppJavascriptBridgeInterface) {
            super();
            mWebAppJavascriptBridgeInterface = webAppJavascriptBridgeInterface;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {

            if (isSchemeJanrain(url)) {
                mWebAppJavascriptBridgeInterface.runJavascript();
                return true;
            }

            return super.shouldOverrideUrlLoading(webView, url);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);

        final String userAgent = webSettings.getUserAgentString();
        webSettings.setUserAgentString("janrainNativeAppBridgeEnabled" + userAgent);

        WebAppInterface webAppJavascriptBridgeInterface = new WebAppInterface(this);
        mWebView.addJavascriptInterface(webAppJavascriptBridgeInterface, ANDROID_NS);

        mWebView.setWebViewClient(new MyWebViewClient(webAppJavascriptBridgeInterface));

        mWebView.loadUrl("http://janrain.github.com/CaptureWebViewDemo/index.html");
    }
}
