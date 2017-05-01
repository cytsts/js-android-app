package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor;

import android.content.Context;
import android.webkit.WebView;

/**
 * Created by aleksandrdakhno on 4/29/17.
 */

public class AdhocDataViewVisualizeExecutor extends VisualizeExecutor implements AdhocDataViewExecutorApi {

    private String resourceUri;

    public AdhocDataViewVisualizeExecutor(Context context, WebView webView, String resourceUri) {
        super(context, webView);
        this.resourceUri = resourceUri;
    }

    /*
     * AdhocDataViewExecutorApi
     */

    @Override
    public void prepare(String baseUrl, Completion completion) {
        super.prepare(baseUrl, completion);
    }

    @Override
    public void setScale(float scale) {
        executeJavascriptCode(javascriptCodeForSetScale(scale));
    }

    @Override
    public void run(Completion completion) {
        completions.put("run", completion);
        executeJavascriptCode(javascriptCodeForRun());
    }

    @Override
    public void refresh() {
        executeJavascriptCode(javascriptCodeForRefresh());
    }

    @Override
    public void destroy() {
//        executeJavascriptCode(javascriptCodeForDestroy());
        webEnvironment.destroy();
    }

    /*
     * Private
     */

    private String javascriptCodeForSetScale(float scale) {
        String executeScript = String.format("javascript:Environment.setScale('%.2f')", scale);
        return executeScript;
    }

    private String javascriptCodeForRun() {
        String executeScript = String.format("javascript:JasperMobile.AdhocDataView.API.run('%s')", resourceUri);
        return executeScript;
    }

    private String javascriptCodeForRefresh() {
        String executeScript = String.format("javascript:run('%s')", resourceUri);
        return executeScript;
    }

    private String javascriptCodeForDestroy() {
        String executeScript = String.format("javascript:destroy('%s')", resourceUri);
        return executeScript;
    }
}
