package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.webviewstore;

import android.webkit.WebView;

/**
 * Created by aleksandrdakhno on 5/3/17.
 */

public class WebviewStore {
    private WebView webView;

    private static WebviewStore instance;
    public static WebviewStore getInstance() {
        if (instance == null) {
            instance = new WebviewStore();
        }
        return instance;
    }

    public void saveWebView(WebView webView) {
        this.webView = webView;
    }

    public WebView getWebView() {
        return webView;
    }
}
