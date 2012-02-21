package com.janrain.capture.webviewpoc;

import android.app.Activity;
import android.graphics.Bitmap;
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
    private WebView mWebView;
    private Button mStartButton;
    private final String mSentinelUrl = "https://webview-poc.dev.janraincapture.com/webview-poc/capture_finish";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "[onCreate]");
        
        setContentView(R.layout.main);
        
        mWebView = (WebView) findViewById(R.id.capture_webview);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mWebView.loadUrl("https://webview-poc.dev.janraincapture.com/oauth/signin_mobile?" +
                        "redirect_uri=" + mSentinelUrl +
                        "&client_id=zc7tx83fqy68mper69mxbt5dfvd7c2jh&response_type=token");
            }
        });
    }
    
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "[shouldOverrideUrlLoading]: " + url);

            if (isSentinelUrl(url)) {
                Log.d(TAG, "Token: " + url);
                processSentinelUrl(url);
                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "[onPageStarted]: " + url);

            if (isSentinelUrl(url)) {
                processSentinelUrl(url);
                view.stopLoading();
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(TAG, "[onReceivedError]");

            String message = "WebView error: " + description + "\nFor " + failingUrl;
            logAndToast(message);
        }

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
        toast(message);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void processSentinelUrl(String url) {
        String message = "Token: " + url;
        toast(message);
        Log.d(TAG, message);
    }

    private boolean isSentinelUrl(String url) {
        return url.startsWith(mSentinelUrl);
    }
}
