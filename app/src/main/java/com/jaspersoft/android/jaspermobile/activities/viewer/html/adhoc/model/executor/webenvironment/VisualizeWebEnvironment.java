package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.webenvironment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.webenvironment.webinterface.VisualizeWebInterfaceListener;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.webenvironment.webinterface.VisualizeWebInterface;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.webenvironment.webinterface.VisualizeWebResponse;
import com.jaspersoft.android.jaspermobile.internal.di.components.AdhocDataViewWebEnvironmentComponent;
import com.jaspersoft.android.jaspermobile.util.VisualizeEndpoint;
import com.jaspersoft.android.jaspermobile.webview.DefaultUrlPolicy;
import com.jaspersoft.android.jaspermobile.webview.JasperWebViewClientListener;
import com.jaspersoft.android.jaspermobile.webview.SystemWebViewClient;
import com.jaspersoft.android.jaspermobile.webview.UrlPolicy;
import com.jaspersoft.android.jaspermobile.webview.WebViewEnvironment;
import com.jaspersoft.android.jaspermobile.webview.dashboard.InjectionRequestInterceptor;
import com.jaspersoft.android.jaspermobile.webview.intercept.VisualizeResourcesInterceptRule;
import com.jaspersoft.android.jaspermobile.webview.intercept.WebResourceInterceptor;
import com.jaspersoft.android.jaspermobile.webview.intercept.okhttp.OkHttpWebResourceInterceptor;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by aleksandrdakhno on 4/29/17.
 */

public class VisualizeWebEnvironment {

    public interface Listener {
        void onEnvironmentReady();
        void onVisualizeReady();
        void onSuccess(String operation, Object data);
        void onFail(String operation, String error);
    }

    @Inject
    @Named("webview_client")
    OkHttpClient webViewResourceClient;

    private Context context;
    private WebView webView;
    private Listener listener;

    public VisualizeWebEnvironment(Context context, WebView webView) {
        getComponent(context).inject(this);
        this.context = context;
        this.webView = webView;
    }

    private AdhocDataViewWebEnvironmentComponent getComponent(Context context) {
        return GraphObject.Factory.from(context)
                .getProfileComponent()
                .plusAdhocDataViewWebEnvironment();
    }

    public Context getContext() {
        return context;
    }

    public WebView getWebView() {
        return webView;
    }

    public void prepare(String baseUrl) {
        prepareWebView(baseUrl);
        prepareEnvironment(baseUrl);
    }

    public void destroy() {
        if (webView != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.removeAllViews();
            webView.destroy();
        }
    }

    public void subscribe(Listener listener) {
        this.listener = listener;
    }

    public void unsubscribe() {
        this.listener = null;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void prepareWebView(String baseUrl) {
        webView.getSettings().setJavaScriptEnabled(true);

        UrlPolicy defaultPolicy = new DefaultUrlPolicy(baseUrl)
                .withSessionListener(new DefaultUrlPolicy.SessionListener() {
                    @Override
                    public void onSessionExpired() {
                        Log.d("AdhocDataViewModel", "onSessionExpired");
                        if (listener != null) {
                            listener.onFail(null, "Session expired");
                        }
                    }
                });

        WebResourceInterceptor injectionRequestInterceptor = InjectionRequestInterceptor.getInstance();

        WebResourceInterceptor.Rule reportResourcesRule = VisualizeResourcesInterceptRule.getInstance();
        WebResourceInterceptor cacheResourceInterceptor = new OkHttpWebResourceInterceptor.Builder()
                .withClient(webViewResourceClient)
                .registerRule(reportResourcesRule)
                .build();

        SystemWebViewClient systemWebViewClient = new SystemWebViewClient.Builder()
                .withDelegateListener(new JasperWebViewClientListenerImpl())
                .registerInterceptor(injectionRequestInterceptor)
                .registerInterceptor(cacheResourceInterceptor)
                .registerUrlPolicy(defaultPolicy)
                .build();

        webView.setWebViewClient(systemWebViewClient);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int newProgress) {
//                if (newProgress == 100) {
//                    mProgressBar.setVisibility(View.GONE);
//                } else {
//                    mProgressBar.setVisibility(View.VISIBLE);
//                    mProgressBar.setProgress(newProgress);
//                }
            }
        });
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        WebViewEnvironment.configure(webView)
                .withWebInterface(
                        VisualizeWebInterface.from(new VisualizeWebInterfaceListenerImpl())
                );
    }

    private void prepareEnvironment(String baseUrl) {
        InputStream stream = null;
        Context context = webView.getContext();

        try {
            stream = context.getAssets().open("adhocDataView.html");
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");

            VisualizeEndpoint endpoint = VisualizeEndpoint.forBaseUrl(baseUrl)
                    .optimized()
                    .build();

            Map<String, String> data = new HashMap<String, String>();
            data.put("visualize_url", endpoint.createUri());
            float scale = calculateScale();
            data.put("body_height_percent", "" + scale * 100);
            data.put("body_width_percent", "" + scale * 100);
            data.put("body_transform_scale", "" + 1 / scale);
            Template tmpl = Mustache.compiler().compile(writer.toString());
            String html = tmpl.execute(data);

            webView.loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    private float calculateScale() {
        float heightDp = convertPixelsToDp(context.getResources().getDisplayMetrics().heightPixels, context);
        float widthDp = convertPixelsToDp(context.getResources().getDisplayMetrics().widthPixels, context);

        final float S_10_1 = 962560;
        final float S_3_2 = 153600;
        float s = widthDp * heightDp;
        float scale = (2 * S_10_1 - S_3_2 - s) / (S_10_1 - S_3_2);

        Log.d("AdhocDataViewModel", "scale: " + scale);
        return scale;
    }

    private static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    private class JasperWebViewClientListenerImpl implements JasperWebViewClientListener {
        @Override
        public void onPageStarted(String newUrl) {
            Log.d("VisualizeWebEnvironment", "onPageStarted url: " + newUrl);
        }

        @Override
        public void onPageFinishedLoading(String onPageFinishedLoading) {
            Log.d("VisualizeWebEnvironment", "onPageFinishedLoading url: " + onPageFinishedLoading);
        }

        @Override
        public void onReceivedError(int errorCode, String description, String failingUrl) {
            Log.d("VisualizeWebEnvironment", "onReceivedError url: " + failingUrl);
        }
    }

    private class VisualizeWebInterfaceListenerImpl implements VisualizeWebInterfaceListener {
        @Override
        public void onEnvironmentReady() {
            Log.d("VisualizeWebEnvironment", "onEnvironmentReady");

            if (listener != null) {
                listener.onEnvironmentReady();
            }
        }

        @Override
        public void onVisualizeReady() {
            Log.d("VisualizeWebEnvironment", "onVisualizeReady");
            if (listener != null) {
                listener.onVisualizeReady();
            }
        }

        @Override
        public void onVisualizeFailed(String error) {
            Log.d("VisualizeWebEnvironment", "onVisualizeFailed: " + error);
            if (listener != null) {
                listener.onFail(null, error);
            }
        }

        @Override
        public void onOperationDone(VisualizeWebResponse response) {
            Log.d("VisualizeWebEnvironment", "onOperationDone");
            if (listener != null) {
                listener.onSuccess(response.getOperation(), response.getParameters());
            }
        }

        @Override
        public void onOperationError(VisualizeWebResponse response) {
            Log.d("VisualizeWebEnvironment", "onOperationError with error: " + response.getError());
            if (listener != null) {
                listener.onFail(response.getOperation(), response.getError());
            }
        }
    }

}
