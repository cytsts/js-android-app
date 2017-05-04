package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.VisualizeWebEnvironment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.VisualizeWebResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleksandrdakhno on 4/24/17.
 */

public class VisualizeExecutor implements VisualizeWebEnvironment.Listener {

    protected VisualizeWebEnvironment webEnvironment;
    protected Map<String, Completion> completions = new HashMap<>();

    public interface Completion {
        void success(Object data);
        void failed(String error);
    }

    protected VisualizeExecutor(Context context, WebView webView, String baseUrl) {
        webEnvironment = new VisualizeWebEnvironment(context, webView, baseUrl);
        webEnvironment.subscribe(this);
    }

    protected void prepare(Completion completion) {
        completions.put("prepare", completion);
        webEnvironment.prepare();
    }

    protected void destroy() {
        webEnvironment.unsubscribe(this);
        webEnvironment.destroy();
    }

    /*
     * VisualizeWebEnvironment.Listener
     */

    @Override
    public void onEventReceived(VisualizeWebResponse response) {
        switch (response.getOperation()) {
            case "onEnvironmentReady":
                new Handler(webEnvironment.getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        prepareVisualize();
                    }
                });
                break;
            case "onVisualizeReady":
                Completion completion = completions.get("prepare");
                if (completion != null) {
                    completion.success(null);
                    completions.remove("prepare");
                }
                break;
            case "onVisualizeFailed": {
                String message = ((Map<String, String>) response.getErrorParameters()).get("message");
                Log.d("VisualizeExecutor", "onVisualizeFailed: " + message);
                break;
            }
        }
    }

    @Override
    public void onOperationDone(VisualizeWebResponse response) {
        Completion completion = completions.get(response.getOperation());
        if (completion != null) {
            if (response.getErrorParameters() != null) {
                String message = ((Map<String, String>) response.getErrorParameters()).get("message");
                completion.failed(message);
            } else {
                completion.success(response.getDataParameters());
            }
            completions.remove(response.getOperation());
        }
    }

    /*
     * Private methods
     */

    protected void executeJavascriptCode(String code) {
        if (code.equals("javascript:JasperMobile.Environment.askIsReady()")) {
            if (webEnvironment.isInitialized()) {
                webEnvironment.getWebView().loadUrl(code);
            } else {
                onOperationDone(
                        new VisualizeWebResponse(
                                VisualizeWebResponse.Type.Operation,
                                "askIsReady",
                                "{isReady:false}",
                                null
                        )
                );
            }
        } else {
            webEnvironment.getWebView().loadUrl(code);
        }
    }

    private void prepareVisualize() {
        webEnvironment.getWebView().loadUrl(stringUrlForPrepareVisualize());
    }

    private String stringUrlForPrepareVisualize() {
        String executeScript = String.format("javascript:JasperMobile.VIZ.prepare()");
        return executeScript;
    }
}
