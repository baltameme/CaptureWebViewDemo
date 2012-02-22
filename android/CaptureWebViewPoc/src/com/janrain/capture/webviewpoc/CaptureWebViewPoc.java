package com.janrain.capture.webviewpoc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class CaptureWebViewPoc extends Activity {
    private static final String TAG = CaptureWebViewPoc.class.getSimpleName();

    // This is the base URL for your Capture domain
    private static final String CAPTURE_URL_BASE = "https://webview-poc.dev.janraincapture.com";
    private WebView mWebView;
    private Button mStartButton;

    /* This URL serves as a sentinel. Capture redirects to it and it is watched for in
     * shouldOverrideUrlLoading. When reached, the token is extracted and the WebView may be closed.
     * Not that *it is not a real URL*, and can't be loaded, it is just a convenient URL to use as a
     * sentinel. */
    private final String mSentinelUrl = CAPTURE_URL_BASE + "/webview-poc/capture_finish";

    /* This URL hits the signin_mobile start point in Capture UI which shows a mobile-optimized sign-in
     * screen. It takes three parameters:
     *  - redirect_uri: the final URL Capture will redirect to, loaded with the sentinel defined above
     *  - client_id: the client ID of your Capture API client for mobile
     *  - response_type: loaded with 'token' to configure an access token to be included in the fragment of
     *    the redirect URI.
      */
    private final String captureStartUrl = CAPTURE_URL_BASE + "/oauth/signin_mobile?" +
            "redirect_uri=" + mSentinelUrl +
            "&client_id=zc7tx83fqy68mper69mxbt5dfvd7c2jh" +
            "&response_type=token";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "[onCreate]");
        
        setContentView(R.layout.main);
        
        mWebView = (WebView) findViewById(R.id.capture_webview);
        mWebView.setWebViewClient(mWebViewClient); // watches URLs as they load
        mWebView.getSettings().setJavaScriptEnabled(true); // may not be necessary, should be on by default

        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mWebView.loadUrl(captureStartUrl);
            }
        });
    }
    
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "[shouldOverrideUrlLoading]: " + url);

            if (isSentinelUrl(url)) {
                processSentinelUrl(url);
                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        /*
         * This is a work-around for pre 2.2 (2.3?) versions of Android, which don't call
         * shouldOverrideUrlLoading before 302 redirects
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "[onPageStarted]: " + url);

            if (isSentinelUrl(url)) {
                processSentinelUrl(url);
                view.stopLoading();
            }
        }

        /*
         * Just some error loggers useful for debugging
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(TAG, "[onReceivedError]");

            String message = "WebView error: " + description + "\nFor " + failingUrl;
            logAndToast(message);
        }

        /*
         * Just some error loggers useful for debugging
         * NOTE that this may automatically proceed when the WebView encounters an invalid cert.
         * DO NOT DEPLOY THIS METHOD TO PRODUCTION
         */
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d(TAG, "[onReceivedSslError]");

            String message = error.toString();
            logAndToast(message);
            handler.proceed();
        }
    };

    private void logAndToast(String message) {
        Log.d(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void processSentinelUrl(String url) {
        // Extract the access token from the fragment of the URL passed in here
        // Example URL:
        // https://webview-poc.dev.janraincapture.com/webview-poc/capture_finish#access_token=et8h5yqwak9qrbeh

        // Assumes the token is the value of the first parameter:
        String token = Uri.parse(url).getFragment().split(";")[0].split("=")[1];

        // There aren't other parameters passed via the fragment but if their were they could need to be
        // URL decoded if they don't use URL safe characters.

        String message = "Token: " + token;
        logAndToast(message);
    }

    private boolean isSentinelUrl(String url) {
        return url.startsWith(mSentinelUrl);
    }
}
