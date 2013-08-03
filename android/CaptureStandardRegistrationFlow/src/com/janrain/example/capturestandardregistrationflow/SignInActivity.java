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
import android.graphics.Bitmap;
import android.view.Window;
import org.json.JSONArray;
import org.json.JSONException;

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
        private static final String LOG_TAG = "WebAppInterface";

        final static String JAVASCRIPT = "javascript:"
                + "(function() {"
                + "  if (typeof createJanrainBridge.eventQueue === 'undefined') return \"undefined queue\";"
                + "  var t = JSON.stringify(createJanrainBridge.eventQueue);"
                + "  window." + ANDROID_NS + ".bridgeCallback(t);"
                + "})();";

        @JavascriptInterface
        public void bridgeCallback(String eventQueueString) {
            Log.d(LOG_TAG, "bridgeCallback: " + eventQueueString.substring(0, Math.min(eventQueueString.length(), 50)));
            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(eventQueueString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "", e);
                return;
            }

            for (int i = 0; i < jsonArray.length(); ++i) {
                String argsUrl;
                try {
                    argsUrl = jsonArray.getString(i);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Invalid Event", e);
                    return;
                }

                if (argsUrl != null && isSchemeJanrain(argsUrl)) {
                    parseAndDispatchEventUrl(argsUrl);
                } else {
                    Log.e(LOG_TAG, "Unknown scheme: " + argsUrl);
                }
            }
        }

        private void showSignInCompleteDialogFragment(final JSONArray jsonArray) {
            String jsonArrayString;
            try {
                jsonArrayString = jsonArray.toString(4);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "", e);
                return;
            }

            AlertDialog.Builder adb = new AlertDialog.Builder(SignInActivity.this);
            adb.setTitle("Sign-In Complete");
            adb.setMessage(jsonArrayString);
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    SignInActivity.this.finish();
                }
            });
            adb.show();
        }

        private void parseAndDispatchEventUrl(final String argsUrl) {
            // argsUrl will look like:
            // janrain:eventNameHere?arguments=URL%20ENCODED%20JSON%20ARRAY%20HERE
            if (argsUrl.charAt(SCHEME_JANRAIN.length()) != ':') {
                Log.e(LOG_TAG, "Missing colon in: " + argsUrl);
                return;
            }

            int start = SCHEME_JANRAIN.length() + 1;
            int indexQuestionMark = argsUrl.indexOf('?', start);
            if (indexQuestionMark == -1) {
                Log.e(LOG_TAG, "Missing question mark in: " + argsUrl);
                return;
            }

            String eventName = argsUrl.substring(start, indexQuestionMark);
            if (TextUtils.isEmpty(eventName)) {
                Log.w(LOG_TAG, "Empty Event");
                return;
            }

            if (eventName.equals("onCaptureLoginSuccess")) {
                processOnCaptureLoginSuccess(argsUrl);
            } else {
                Log.d(LOG_TAG, "Unhandled event: " + eventName);
            }
        }

        private void processOnCaptureLoginSuccess(String argsUrl) {
            final String uriString = Uri.decode(argsUrl);
            if (uriString == null) {
                Log.e(LOG_TAG, "url parsing error for: " + argsUrl);
                return;
            }

            final int index = uriString.indexOf("arguments=");
            if (index < 0) {
                Log.e(LOG_TAG, "missing argument param for: " + uriString);
                return;
            }

            final int start = index + "arguments=".length();
            String jsonValue = uriString.substring(start);

            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(jsonValue);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "", e);
                return;
            }

            showSignInCompleteDialogFragment(jsonArray);
            CaptureStandardRegistrationFlowDemo appState = (CaptureStandardRegistrationFlowDemo) getApplication();
            appState.getCaptureLoginSuccessEventSubject().setEventData(jsonArray);
            appState.getCaptureLoginSuccessEventSubject().fireEvent();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        private static final String TAG = "MyWebViewClient";

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            Log.w(TAG, "shouldOverrideUrlLoading: " + url);
            if (isSchemeJanrain(url)) {
                Log.d(WebAppInterface.LOG_TAG, "Dispatching JS fetcher");
                mWebView.loadUrl(WebAppInterface.JAVASCRIPT);
                return true;
            }

            return super.shouldOverrideUrlLoading(webView, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            setProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_signin);

        mWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);

        webSettings.setUserAgentString(webSettings.getUserAgentString() + " janrainNativeAppBridgeEnabled");

        mWebView.addJavascriptInterface(new WebAppInterface(), ANDROID_NS);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl("http://janrain.github.com/CaptureWebViewDemo/index.html");
    }
}
