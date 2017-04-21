package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.webview.WebInterface;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

class AdhocDataViewWebInterface extends WebInterface implements AdhocDataViewCallback {

    private final AdhocDataViewCallback delegate;

    private AdhocDataViewWebInterface(AdhocDataViewCallback delegate) {
        this.delegate = delegate;
    }

    public static WebInterface from(AdhocDataViewCallback callback) {
        return new AdhocDataViewWebInterface(callback);
    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    @Override
    public void exposeJavascriptInterface(WebView webView) {
        webView.addJavascriptInterface(this, "Android");
    }

    /*
     * Adhoc Callback Interface
     */

    @JavascriptInterface
    @Override
    public void onScriptLoaded() {
        handleCallback(new Runnable() {
            @Override
            public void run() {
                delegate.onScriptLoaded();
            }
        });
    }

    @JavascriptInterface
    @Override
    public void onLoadStart() {
        handleCallback(new Runnable() {
            @Override
            public void run() {
                delegate.onLoadStart();
            }
        });
    }

    @JavascriptInterface
    @Override
    public void onLoadDone() {
        handleCallback(new Runnable() {
            @Override
            public void run() {
                delegate.onLoadDone();
            }
        });
    }

    @JavascriptInterface
    @Override
    public void onLoadError(final String error) {
        handleCallback(new Runnable() {
            @Override
            public void run() {
                delegate.onLoadError(error);
            }
        });
    }
}
