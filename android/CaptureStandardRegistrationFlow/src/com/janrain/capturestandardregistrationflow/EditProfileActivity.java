package com.janrain.capturestandardregistrationflow;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class EditProfileActivity extends Activity {

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

    final private class JavascriptBridgeInterface {
        public JavascriptBridgeInterface() {
        }
    }

    private class MyWebViewClient extends WebViewClient {
        // private static final String LOG_TAG = "MyWebViewClient";

        private JavascriptBridgeInterface mJavascriptBridgeInterface;

        /**
         * @param mJavascriptBridgeInterface
         */
        public MyWebViewClient(
                JavascriptBridgeInterface javascriptBridgeInterface) {
            super();
            mJavascriptBridgeInterface = javascriptBridgeInterface;
        }
    }

    private static final String ANDROID_NS = "demo";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);

        final String userAgent = webSettings.getUserAgentString();
        webSettings.setUserAgentString("janrainNativeAppBridgeEnabled"
                + userAgent);

        final JavascriptBridgeInterface javascriptBridgeInterface = new JavascriptBridgeInterface();
        mWebView.addJavascriptInterface(javascriptBridgeInterface, ANDROID_NS);

        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient(javascriptBridgeInterface));

        final String url = getString(R.string.url_edit_profile);
        mWebView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_profile, menu);
        return true;
    }
}
