package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.executor;

import android.webkit.WebView;

/**
 * Created by aleksandrdakhno on 4/24/17.
 */

public class AdhocDataViewExecutor {

    private WebView webView;
    private String resourceUri;

    public AdhocDataViewExecutor(WebView webView, String resourceUri) {
        this.webView = webView;
        this.resourceUri = resourceUri;
    }

    public void setScale(float scale) {
        webView.loadUrl(stringUrlForSetScale(scale));
    }

    public void run() {
        webView.loadUrl(stringUrlForRun());
    }

    public void refresh() {
        webView.loadUrl(stringUrlForRefresh());

    }

    public void destroy() {
        webView.loadUrl(stringUrlForDestroy());
    }

    /*
     * Private methods
     */

    private String stringUrlForSetScale(float scale) {
        String executeScript = String.format("javascript:Environment.setScale('%.2f')", scale);
        return executeScript;
    }

    private String stringUrlForRun() {
        // JasperMobile.VIZ.AdhocDataView.API.run()
        String executeScript = String.format("javascript:JasperMobile.VIZ.AdhocDataView.API.run('%s')", resourceUri);
        return executeScript;
    }

    private String stringUrlForRefresh() {
        String executeScript = String.format("javascript:run('%s')", resourceUri);
        return executeScript;
    }

    private String stringUrlForDestroy() {
        String executeScript = String.format("javascript:run('%s')", resourceUri);
        return executeScript;
    }
}
