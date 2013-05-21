package com.janrain.capturestandardregistrationflow;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SignInActivity extends FragmentActivity implements
SignInCompleteDialogFragment.NoticeDialogListener {
    private static final String ANDROID_NS = "demo";
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

    /*
     * (non-Javadoc)
     * 
     * WebAppInterface class binds WebView to Javascript. 
     * For futher details, 
     * 
     * @see
     * http://developer.android.com/guide/webapps/webview.html#BindingJavaScript
     */
    public class WebAppInterface {
        private static final String SIGNIN_COMPLETE_TAG = "signin complete";
        private static final String LOG_TAG = "WebAppInterface";

        final static String JAVASCRIPT = "javascript:"
                + "(function() {"
                + "if (typeof createJanrainBridge.eventQueue === 'undefined') return \"undefined queue\";"
                + "var t = JSON.stringify(createJanrainBridge.eventQueue);"
                + "window." + ANDROID_NS + ".bridgeCallback(t);" + "})();";

        private Context mContext = null;

        /** Instantiate the interface and set the context */
        public WebAppInterface(Context context) {
            mContext = context;
        }

        public void runJavascript() {
            mHandler.post(new Runnable() {
                @Override
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
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction ft = fragmentManager.beginTransaction();

            final Fragment prev = fragmentManager
                    .findFragmentByTag(SIGNIN_COMPLETE_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            String jsonArrayString = jsonArray.toString();
            try {
                jsonArrayString = jsonArray.toString(4);
            } catch (JSONException e) {
            }
            final DialogFragment dialogFrag = new SignInCompleteDialogFragment(
                    jsonArrayString);

            dialogFrag.show(fragmentManager, SIGNIN_COMPLETE_TAG);
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
                            CaptureStandardRegistrationFlow appState = 
                                    (CaptureStandardRegistrationFlow) getApplication();
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
        private static final String LOG_TAG = "MyWebViewClient";

        private WebAppInterface mWebAppJavascriptBridgeInterface;

        /**
         * @param mJavascriptBridgeInterface
         */
        public MyWebViewClient(
                WebAppInterface webAppJavascriptBridgeInterface) {
            super();
            mWebAppJavascriptBridgeInterface = webAppJavascriptBridgeInterface;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit
         * .WebView, java.lang.String)
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            boolean result = super.shouldOverrideUrlLoading(webView, url);

            if (isSchemeJanrain(url)) {
                mWebAppJavascriptBridgeInterface.runJavascript();
            }

            return result;
        }
    }

    /**
     * Provides a hook for calling "alert" from Javascript. Useful for debugging
     * your Javascript.
     */
    final class MyWebChromeClient extends WebChromeClient {
        // @Override
        // public boolean onJsAlert(WebView view, String url, String message,
        // JsResult result) {
        // Log.d(LOG_TAG, message);
        // result.confirm();
        // return true;
        // }
    }

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

        WebAppInterface webAppJavascriptBridgeInterface = new WebAppInterface(this);
        mWebView.addJavascriptInterface(webAppJavascriptBridgeInterface, ANDROID_NS);

        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient(webAppJavascriptBridgeInterface));

        final String url = getString(R.string.url_index);
        mWebView.loadUrl(url);
        // mWebView.loadUrl("file:///android_asset/demo.html");
        // javascriptBridgeInterface.runJavascript();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDialogDismiss(DialogFragment dialogFragment) {
        finish();
    }
}
