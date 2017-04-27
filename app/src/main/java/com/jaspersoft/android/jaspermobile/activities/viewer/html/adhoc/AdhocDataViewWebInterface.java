package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.jaspersoft.android.jaspermobile.webview.WebInterface;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

class AdhocDataViewWebInterface extends WebInterface {

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
    public void postMessage(String message) {
        Log.d("ADViewWebInterface", "message: " + message);
        JavascriptMessage javascriptMessage = new Gson().fromJson(message, JavascriptMessage.class);
        switch (javascriptMessage.getType()) {
            case CALLBACK: {
                processCallback(javascriptMessage.getCommand(), javascriptMessage.getParameters());
                break;
            }
            case LISTENER: {
                Log.d("ADViewWebInterface", "listener for: " + javascriptMessage.getCommand());
                break;
            }
        }
    }

    private void processCallback(final String command, final Object parameters) {
        handleCallback(new Runnable() {
            @Override
            public void run() {
                switch (command) {
                    case "onEnvironmentReady":
                        delegate.onEnvironmentReady();
                        break;
                    case "onLoadStart":
                        delegate.onLoadStart();
                        break;
                    case "onLoadDone":
                        delegate.onLoadDone();
                        break;
                    case "onLoadError":
                        // TODO: get error from parameters
                        delegate.onLoadError(null);
                        break;
                    default:
                        throw new RuntimeException("Unsupported command");
                }
            }
        });
    }
}
