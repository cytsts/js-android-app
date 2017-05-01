package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor;

import android.content.Context;
import android.os.Handler;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.webenvironment.VisualizeWebEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleksandrdakhno on 4/24/17.
 */

public class VisualizeExecutor implements VisualizeWebEnvironment.Listener {

    protected VisualizeWebEnvironment webEnvironment;
    protected Map<String, Completion> completions = new HashMap<>();

    public interface Completion {
        void success();
        void failed(String error);
    }

    protected VisualizeExecutor(Context context, WebView webView) {
        webEnvironment = new VisualizeWebEnvironment(context, webView);
    }

    protected void prepare(String baseUrl, Completion completion) {
        completions.put("prepare", completion);
        webEnvironment.subscribe(this);
        webEnvironment.prepare(baseUrl);
    }

    /*
     * VisualizeWebEnvironment.Listener
     */

    @Override
    public void onEnvironmentReady() {
        new Handler(webEnvironment.getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                prepareVisualize();
            }
        });
    }

    @Override
    public void onVisualizeReady() {
        Completion completion = completions.get("prepare");
        if (completion != null) {
            completion.success();
            completions.remove("prepare");
        }
    }

    @Override
    public void onSuccess(String operation) {
        Completion completion = completions.get(operation);
        if (completion != null) {
            completion.success();
            completions.remove(operation);
        }
    }

    @Override
    public void onFail(String operation, String error) {
        Completion completion = completions.get(operation);
        if (completion != null) {
            completion.failed(error);
            completions.remove(operation);
        }
    }

    /*
     * Private methods
     */

    protected void executeJavascriptCode(String code) {
        webEnvironment.getWebView().loadUrl(code);
    }

    private void prepareVisualize() {
        webEnvironment.getWebView().loadUrl(stringUrlForPrepareVisualize());
    }

    private String stringUrlForPrepareVisualize() {
        String executeScript = String.format("javascript:JasperMobile.VIZ.prepare()");
        return executeScript;
    }
}
