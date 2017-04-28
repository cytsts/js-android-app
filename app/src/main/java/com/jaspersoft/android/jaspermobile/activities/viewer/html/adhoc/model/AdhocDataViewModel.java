package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.AdhocDataViewWebInterfaceListener;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.AdhocDataViewController;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.AdhocDataViewWebInterface;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.executor.AdhocDataViewExecutor;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.internal.di.components.AdhocDataViewModelComponent;
import com.jaspersoft.android.jaspermobile.util.VisualizeEndpoint;
import com.jaspersoft.android.jaspermobile.webview.DefaultUrlPolicy;
import com.jaspersoft.android.jaspermobile.webview.JasperWebViewClientListener;
import com.jaspersoft.android.jaspermobile.webview.SystemWebViewClient;
import com.jaspersoft.android.jaspermobile.webview.UrlPolicy;
import com.jaspersoft.android.jaspermobile.webview.WebInterface;
import com.jaspersoft.android.jaspermobile.webview.WebViewEnvironment;
import com.jaspersoft.android.jaspermobile.webview.dashboard.InjectionRequestInterceptor;
import com.jaspersoft.android.jaspermobile.webview.intercept.VisualizeResourcesInterceptRule;
import com.jaspersoft.android.jaspermobile.webview.intercept.WebResourceInterceptor;
import com.jaspersoft.android.jaspermobile.webview.intercept.okhttp.OkHttpWebResourceInterceptor;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
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
 * Created by aleksandrdakhno on 4/28/17.
 */

public class AdhocDataViewModel implements JasperWebViewClientListener, DefaultUrlPolicy.SessionListener, AdhocDataViewWebInterfaceListener, AdhocDataViewController {

    @Inject
    JasperServer mServer;

    @Inject
    @Named("webview_client")
    OkHttpClient webViewResourceClient;

    private Context context;
    private WebView mWebView;
    private ResourceLookup resourceLookup;

    private WebInterface mWebInterface;
    private AdhocDataViewExecutor executor;

    private AdhocDataViewModelListener listener;

    public AdhocDataViewModel(Context context, WebView mWebView, ResourceLookup resourceLookup) {
        this.mWebView = mWebView;
        this.context = context;
        this.resourceLookup = resourceLookup;

        getComponent().inject(this);
    }

    public void prepare() {
        prepareWebView();
        prepareEnvironment();

        executor = new AdhocDataViewExecutor(mWebView, resourceLookup.getUri());
    }

    private AdhocDataViewModelComponent getComponent() {
        return GraphObject.Factory.from(context)
                .getProfileComponent()
                .plusAdhocDataViewModel();
    }

    /*
     * AdhocDataViewController
     */

    @Override
    public void subscribe(AdhocDataViewModelListener listener) {
        this.listener = listener;
    }

    @Override
    public void unsubscribe(AdhocDataViewModelListener listener) {
        this.listener = null;
    }

    @Override
    public void run() {
        if (listener != null) {
            listener.onPreparingStart();
        }
        executor.run();
    }

    /*
     * Private
     */

    private void prepareWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);

        UrlPolicy defaultPolicy = new DefaultUrlPolicy(mServer.getBaseUrl())
                .withSessionListener(this);
        WebResourceInterceptor injectionRequestInterceptor = InjectionRequestInterceptor.getInstance();

        WebResourceInterceptor.Rule reportResourcesRule = VisualizeResourcesInterceptRule.getInstance();
        WebResourceInterceptor cacheResourceInterceptor = new OkHttpWebResourceInterceptor.Builder()
                .withClient(webViewResourceClient)
                .registerRule(reportResourcesRule)
                .build();

        SystemWebViewClient systemWebViewClient = new SystemWebViewClient.Builder()
                .withDelegateListener(this)
                .registerInterceptor(injectionRequestInterceptor)
                .registerInterceptor(cacheResourceInterceptor)
                .registerUrlPolicy(defaultPolicy)
                .build();

        mWebView.setWebViewClient(systemWebViewClient);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int newProgress) {
//                if (newProgress == 100) {
//                    mProgressBar.setVisibility(View.GONE);
//                } else {
//                    mProgressBar.setVisibility(View.VISIBLE);
//                    mProgressBar.setProgress(newProgress);
//                }
            }
        });
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);

        mWebInterface = AdhocDataViewWebInterface.from(this);
        WebViewEnvironment.configure(mWebView)
                .withWebInterface(mWebInterface);
    }

    private void prepareEnvironment() {
        InputStream stream = null;
        Context context = mWebView.getContext();

        try {
            stream = context.getAssets().open("adhocDataView.html");
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");

            VisualizeEndpoint endpoint = VisualizeEndpoint.forBaseUrl(mServer.getBaseUrl())
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

            mWebView.loadDataWithBaseURL(mServer.getBaseUrl(), html, "text/html", "utf-8", null);
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

    /*
     * AdhocDataViewWebInterfaceListener
     */

    @Override
    public void onEnvironmentReady() {
        Log.d("AdhocDataViewModel", "onEnvironmentReady");
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                executor.prepareVisualize();
            }
        });
    }

    @Override
    public void onVisualizeReady() {
        Log.d("AdhocDataViewModel", "onVisualizeReady");
        if (listener != null) {
            listener.onPreparingEnd();
        }
    }

    @Override
    public void onVisualizeFailed(String error) {
        Log.d("AdhocDataViewModel", "onVisualizeFailed: " + error);
        if (listener != null) {
            listener.onPreparingFailed();
        }
    }

    @Override
    public void onOperationDone() {
        Log.d("AdhocDataViewModel", "onOperationDone");
        if (listener != null) {
            listener.onOperationEnd();
        }
    }

    @Override
    public void onOperationError(String error) {
        Log.d("AdhocDataViewModel", "onOperationError with error: " + error);
        if (listener != null) {
            listener.onOperationFailed();
        }
    }

    /*
     * JasperWebViewClientListener
     */

    @Override
    public void onPageStarted(String newUrl) {
        Log.d("AdhocDataViewModel", "onPageStarted url: " + newUrl);
        if (listener != null) {
            listener.onPreparingStart();
        }
    }

    @Override
    public void onReceivedError(int errorCode, String description, String failingUrl) {
        Log.d("AdhocDataViewModel", "onReceivedError url: " + failingUrl);
        if (listener != null) {
            listener.onPreparingFailed();
        }
    }

    @Override
    public void onPageFinishedLoading(String onPageFinishedLoading) {
        Log.d("AdhocDataViewModel", "onPageFinishedLoading url: " + onPageFinishedLoading);
    }

    /*
     * DefaultUrlPolicy.SessionListener
     */

    @Override
    public void onSessionExpired() {
//        Toast.makeText(AdhocDataViewFragment.this.getContext(), R.string.da_session_expired, Toast.LENGTH_SHORT).show();
        Log.d("AdhocDataViewModel", "onSessionExpired");
    }
}
