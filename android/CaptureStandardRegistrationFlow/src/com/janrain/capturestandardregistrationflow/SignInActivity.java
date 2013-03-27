package com.janrain.capturestandardregistrationflow;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SignInActivity extends Activity {
    private static final String SCHEME_JANRAIN = "janrain";

    private static final boolean isSchemeJanrain(final String url) {
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

    public class WebAppInterface {
        private static final String LOG_TAG = "WebAppInterface";

        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context context) {
            mContext = context;
        }

        private void parseAndDispatchEventUrl(final String argsUrl) {
            // argsUrl will look like:
            // janrain:eventNameHere?arguments=URL%20ENCODED%20JSON%20ARRAY%20HERE

            if (isSchemeJanrain(argsUrl)) {
                String eventName = null;

                final int schemeJanrainLength = SCHEME_JANRAIN.length();
                final char colon = argsUrl.charAt(schemeJanrainLength);
                if (colon == ':') {
                    final int start = schemeJanrainLength+1;
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
                        final String ARGUMENTS = "arguments";

                        final Uri uri = Uri.parse(argsUrl);
                        final String paramKey = uri
                                .getQueryParameter(ARGUMENTS);
                        final String urlEncodedJsonValue = uri
                                .getQueryParameter(paramKey);
                        String jsonValue = null;
                        try {
                            jsonValue = URLDecoder.decode(urlEncodedJsonValue,
                                    "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                        }
                        if (jsonValue == null) {
                            Log.e(LOG_TAG, "Invalid URL Encoding");
                        } else {
                            JSONArray jArray = null;
                            try {
                                jArray = new JSONArray(jsonValue);
                            } catch (JSONException e) {
                            }
                            if (jArray == null) {
                                Log.e(LOG_TAG, "Invalid JSON Array format");
                            } else {
                                CaptureStandardRegistrationFlow appState = (CaptureStandardRegistrationFlow) mContext
                                        .getApplicationContext();
                                final EventSubject eventSubject = appState
                                        .getEventSubject();
                                eventSubject.setEventData(jArray);
                                final Thread t = new Thread(eventSubject);
                                t.start();
                            }
                        }
                    } else {
                        Log.i(LOG_TAG, "Ignored Event: " + eventName);
                    }
                }
            } else {
                Log.w(LOG_TAG, "Unknown Scheme");
            }
        }

        void dispatchEventQueue(final String eventQueueString) {
            JSONArray jArray = null;
            try {
                jArray = new JSONArray(eventQueueString);
            } catch (JSONException e) {
            }
            if (jArray == null) {
                Log.e(LOG_TAG, "Empty event queue");
            } else {
                final int length = jArray.length();
                for (int i=0; i<length; ++i) {
                    String argsUrl = null;
                    try {
                        argsUrl = jArray.getString(i);
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

    final public class JavascriptBridgeInterface {
        final static String JAVASCRIPT = "javascript:"
            + "(function() {"
            + "if (typeof createJanrainBridge.eventQueue === 'undefined') return \"undefined queue\";"
            + "var t = JSON.stringify(createJanrainBridge.eventQueue);"
            + "window." + ANDROID_NS + ".bridgeCallback(t);"
            + "})();";

        Context mContext = null;

        public JavascriptBridgeInterface(final Context context) {
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
            final WebAppInterface webAppInterface = new WebAppInterface(mContext);
            webAppInterface.dispatchEventQueue(eventQueueString);
        }
    }

    public class MyWebViewClient extends WebViewClient {
        // private static final String LOG_TAG = "MyWebViewClient";
        
        private JavascriptBridgeInterface mJavascriptBridgeInterface;

        /**
        * @param mJavascriptBridgeInterface
        */
        public MyWebViewClient(JavascriptBridgeInterface javascriptBridgeInterface) {
            super();
            mJavascriptBridgeInterface = javascriptBridgeInterface;
        }

        /* (non-Javadoc)
         * @see android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit.WebView, java.lang.String)
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            boolean result = super.shouldOverrideUrlLoading(webView, url);

            if (isSchemeJanrain(url)) {
                mJavascriptBridgeInterface.runJavascript();                
            }

            return result;
        }
    }

    /**
     * Provides a hook for calling "alert" from Javascript. Useful for
     * debugging your Javascript.
     */
    final class MyWebChromeClient extends WebChromeClient {
//        @Override
//        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//            Log.d(LOG_TAG, message);
//            result.confirm();
//            return true;
//        }
    }

    private static final String ANDROID_NS = "demo";

    private WebView mWebView;

    private Handler mHandler = new Handler();

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

        JavascriptBridgeInterface javascriptBridgeInterface = new JavascriptBridgeInterface(this);
        mWebView.addJavascriptInterface(javascriptBridgeInterface, ANDROID_NS);

        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient(javascriptBridgeInterface));

        final String url = getString(R.string.url_index);
        mWebView.loadUrl(url);
        //mWebView.loadUrl("file:///android_asset/demo.html");
        //javascriptBridgeInterface.runJavascript();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
