package com.jaspersoft.android.jaspermobile.activities.viewer.html.webresource;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.jaspersoft.android.jaspermobile.R;

/**
 * Created by aleksandrdakhno on 2/2/17.
 */

public class WebResourceFragment extends Fragment {

    private static final String ARG_URI = "photo_page_url";

    private Uri mUri;
    private WebView mWebView;

    public static WebResourceFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        WebResourceFragment fragment = new WebResourceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mUri = getArguments().getParcelable(ARG_URI);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_web_resource, container, false);
        mWebView = (WebView) v.findViewById(R.id.fragment_web_resource_web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                Log.d("WebResourceFragment", "shouldOverrideUrlLoading: " + url);
                return false;
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);

        String resourceUrl = mUri.toString();
        if (isResourceViewerUrl(resourceUrl)) {
            resourceUrl = constructUrlForResourceViewer(resourceUrl);
        }
        mWebView.loadUrl(resourceUrl);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.open_in_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.openInItem: {
                Intent i = new Intent(Intent.ACTION_VIEW, mUri);
                startActivity(i);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * WebView URL Helpers
     */
    private boolean isResourceViewerUrl(String url) {
        return url.contains("viewer.html");
    }

    private String constructUrlForResourceViewer(String url) {
        String nodecoration ="sessionDecorator=no&decorate=no";
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        String query = uri.getQuery();
        String fragment = uri.getFragment();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(scheme)
                .encodedAuthority(host + ":" + port)
                .path(path)
                .encodedQuery(nodecoration + query)
                .encodedFragment(fragment);
        String newUrl = builder.build().toString();
        return newUrl;
    }
}
