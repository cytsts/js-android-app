package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.executor.AdhocDataViewExecutor;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.internal.di.components.AdhocDataViewFragmentComponent;
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

//import org.androidannotations.annotations.Bean;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public class AdhocDataViewFragment extends Fragment implements JasperWebViewClientListener, DefaultUrlPolicy.SessionListener, AdhocDataViewCallback {

    static final String ARG_RESOURCE_LOOKUP = "resource_lookup";

    @Inject
    JasperServer mServer;
    @Inject
    @Named("webview_client")
    OkHttpClient webViewResourceClient;

//    @Bean
//    protected ScrollableTitleHelper scrollableTitleHelper;

    private ResourceLookup resourceLookup;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private WebInterface mWebInterface;
    private AdhocDataViewExecutor executor;

    public static AdhocDataViewFragment newInstance(ResourceLookup resource) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_RESOURCE_LOOKUP, resource);
        AdhocDataViewFragment fragment = new AdhocDataViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        resourceLookup = getArguments().getParcelable(ARG_RESOURCE_LOOKUP);
//        scrollableTitleHelper.injectTitle(resourceLookup.getLabel());
        Log.d("AdhocDataViewFragment", "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_adhoc_data_view, container, false);
        mProgressBar = (ProgressBar) v.findViewById(R.id.fragment_adhoc_data_view_progress_bar);
        mProgressBar.setMax(100);
        mWebView = (WebView) v.findViewById(R.id.fragment_adhoc_data_view_web_view);

        getComponent().inject(this);
        prepareWebView();
        prepareEnvironment();

        executor = new AdhocDataViewExecutor(mWebView, resourceLookup.getUri());

        return v;
    }

    /*
     * Preparing
     */

    public AdhocDataViewFragmentComponent getComponent() {
        return GraphObject.Factory.from(getContext())
                .getProfileComponent()
                .plusAdhocDataViewPage();
    }

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
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
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
        float heightDp = convertPixelsToDp(getResources().getDisplayMetrics().heightPixels, getContext());
        float widthDp = convertPixelsToDp(getResources().getDisplayMetrics().widthPixels, getContext());

        final float S_10_1 = 962560;
        final float S_3_2 = 153600;
        float s = widthDp * heightDp;
        float scale = (2 * S_10_1 - S_3_2 - s) / (S_10_1 - S_3_2);

        Log.d("AdhocDataViewFragment", "scale: " + scale);
        return scale;
    }

    private static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    /*
     * JasperWebViewClientListener
     */

    @Override
    public void onPageStarted(String newUrl) {
        showLoading();
        Log.d("AdhocDataViewFragment", "onPageStarted url: " + newUrl);
    }

    @Override
    public void onReceivedError(int errorCode, String description, String failingUrl) {
        Log.d("AdhocDataViewFragment", "onReceivedError url: " + failingUrl);
    }

    @Override
    public void onPageFinishedLoading(String onPageFinishedLoading) {
        Log.d("AdhocDataViewFragment", "onPageFinishedLoading url: " + onPageFinishedLoading);
    }

    /*
     * Loading helpers
     */

    protected void showLoading() {
        ProgressDialogFragment.builder(getFragmentManager())
                .setLoadingMessage(R.string.adv_loading)
                .show();
    }

    protected void hideLoading() {
        ProgressDialogFragment.dismiss(getFragmentManager());
    }

    /*
     * DefaultUrlPolicy.SessionListener
     */

    @Override
    public void onSessionExpired() {
        Toast.makeText(AdhocDataViewFragment.this.getContext(), R.string.da_session_expired, Toast.LENGTH_SHORT).show();
        Log.d("AdhocDataViewFragment", "onSessionExpired");
    }

    /*
     * AdhocDataViewCallback
     */

    @Override
    public void onEnvironmentReady() {
        Log.d("AdhocDataViewFragment", "onEnvironmentReady");

        new Handler(getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                executor.run();
            }
        });

    }

    @Override
    public void onLoadStart() {
        Log.d("AdhocDataViewFragment", "onLoadStart");
        showLoading();
    }

    @Override
    public void onLoadDone() {
        Log.d("AdhocDataViewFragment", "onLoadDone");
        hideLoading();
    }

    @Override
    public void onLoadError(String error) {
        Log.d("AdhocDataViewFragment", "onLoadError: " + error);
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG)
                .show();
        hideLoading();
    }
}
