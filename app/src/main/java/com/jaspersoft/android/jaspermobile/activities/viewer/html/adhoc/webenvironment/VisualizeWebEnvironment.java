package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.GraphObject;
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
        void onEventReceived(VisualizeWebResponse response);
        void onOperationDone(VisualizeWebResponse response);
    }

    @Inject
    @Named("webview_client")
    OkHttpClient webViewResourceClient;

    private Context context;
    private String baseUrl;
    private static boolean isInitialized; // TODO: remove this hack

    public VisualizeWebEnvironment(Context context, String baseUrl) {
        getComponent(context).inject(this);
        this.context = context;
        this.baseUrl = baseUrl;
        prepareWebView(baseUrl);
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
        WebView webView = WebviewStore.getInstance().getWebView();
        if (webView == null) {
            webView = new WebView(context);
            WebviewStore.getInstance().saveWebView(webView);
        }
        return webView;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void prepare() {
        prepareEnvironment(baseUrl);
    }

    public void destroy() {
        ((ViewGroup) getWebView().getParent()).removeView(getWebView());
    }

    public void subscribe(Listener listener) {
        VisualizeWebInterface webInterface = VisualizeWebInterface.getInstance();
        webInterface.addListener(listener);
        WebViewEnvironment.configure(getWebView())
                .withWebInterface(webInterface);
    }

    public void unsubscribe(Listener listener) {
        VisualizeWebInterface.getInstance().removeListener(listener);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void prepareWebView(String baseUrl) {
        getWebView().getSettings().setJavaScriptEnabled(true);
        getWebView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        getWebView().getSettings().setBuiltInZoomControls(true);
        getWebView().getSettings().setDisplayZoomControls(false);
        getWebView().getSettings().setLoadWithOverviewMode(true);
        getWebView().getSettings().setUseWideViewPort(true);

        UrlPolicy defaultPolicy = new DefaultUrlPolicy(baseUrl)
                .withSessionListener(new DefaultUrlPolicy.SessionListener() {
                    @Override
                    public void onSessionExpired() {
                        Log.d("VisualizeWebEnvironment", "onSessionExpired");
                        // TODO: come up with this
//                        if (listener != null) {
//                            Map<String, String> error = new HashMap<>();
//                            error.put("message", "Session expired");
//                            listener.onEventReceived(
//                                    new VisualizeWebResponse(
//                                            VisualizeWebResponse.Type.Event,
//                                            "session_expired",
//                                            null,
//                                            error
//                                    )
//                            );
//                        }
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

        getWebView().setWebViewClient(systemWebViewClient);
    }

    private void prepareEnvironment(String baseUrl) {
        InputStream stream = null;

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

            getWebView().loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null);
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

        Log.d("VisualizeWebEnvironment", "scale: " + scale);
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
            isInitialized = true;
        }

        @Override
        public void onReceivedError(int errorCode, String description, String failingUrl) {
            Log.d("VisualizeWebEnvironment", "onReceivedError url: " + failingUrl);
        }
    }
}
