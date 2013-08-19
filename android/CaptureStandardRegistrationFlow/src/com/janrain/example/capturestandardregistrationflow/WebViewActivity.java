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
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {
    private static final String ANDROID_NS = "demo";
    private static final String SCHEME_JANRAIN = "janrain";
    public static final String SIGN_IN_URL = "http://janrain.github.com/CaptureWebViewDemo/index.html";
    public static final String EDIT_PROFILE_URL = "http://janrain.github.com/CaptureWebViewDemo/edit-profile.html";

    private WebView mWebView;

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
                String argsUrl = jsonArray.optString(i);
                // argsUrl will look like:
                // janrain:eventNameHere?arguments=URL%20ENCODED%20JSON%20ARRAY%20OF%20ARGUMENTS%20HERE

                if (argsUrl == null) {
                    Log.e(LOG_TAG, "bad URL: " + argsUrl);
                    continue;
                }

                Uri parsedUri = Uri.parse(argsUrl);
                if (!"janrain".equals(parsedUri.getScheme())) {
                    Log.e(LOG_TAG, "bad URI: " + parsedUri);
                    continue;
                }

                // XXX Stick a slash in it so it's "hierarchical", according to Uri, so we can use the query param
                // functions
                parsedUri = Uri.parse(parsedUri.getScheme() + ":/" + parsedUri.getEncodedSchemeSpecificPart());
                if ("onCaptureLoginSuccess".equals(parsedUri.getLastPathSegment())) {
                    processOnCaptureLoginSuccess(parsedUri);
                } else {
                    Log.d(LOG_TAG, "Unhandled event: " + parsedUri.getLastPathSegment());
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

            AlertDialog.Builder adb = new AlertDialog.Builder(WebViewActivity.this);
            adb.setTitle("Sign-In Complete");
            adb.setMessage(jsonArrayString);
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    WebViewActivity.this.finish();
                }
            });
            adb.show();
        }

        private void processOnCaptureLoginSuccess(Uri parsedUri) {
            Log.d(LOG_TAG, "WebAppInterface#processOnCaptureLoginSuccess");

            String jsonEncodedArgs = parsedUri.getQueryParameter("arguments");

            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(jsonEncodedArgs);
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

            Uri parsedUrl = Uri.parse(url);
            if ("janrain".equals(parsedUrl.getScheme())) {
                Log.d(TAG, "Dispatching JS fetcher");
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
        String intentUrl = getIntent().getExtras().getString("url");
        if (intentUrl != null) {
            mWebView.loadUrl(intentUrl);
        } else {
            mWebView.loadUrl(SIGN_IN_URL);
        }
    }
}
