package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.AdhocDataViewModel;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.internal.di.components.AdhocDataViewFragmentComponent;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;


/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public class AdhocDataViewFragment extends Fragment implements AdhocDataViewModelListener {

    static final String ARG_RESOURCE_LOOKUP = "resource_lookup";

    private WebView webView;
    private AdhocDataViewModel model;

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_adhoc_data_view, container, false);
        webView = (WebView) v.findViewById(R.id.fragment_adhoc_data_view_web_view);

        ResourceLookup resourceLookup = getArguments().getParcelable(ARG_RESOURCE_LOOKUP);
        assert resourceLookup != null;

        model = new AdhocDataViewModel(this.getContext(), webView, resourceLookup);

        getComponent().inject(this);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(resourceLookup.getLabel());
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        model.subscribe(this);
        model.prepare();
    }

    @Override
    public void onStop() {
        super.onStop();
        model.unsubscribe(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.removeAllViews();
            webView.destroy();
        }
    }

    private AdhocDataViewFragmentComponent getComponent() {
        return GraphObject.Factory.from(getContext())
                .getProfileComponent()
                .plusAdhocDataViewPage();
    }

    /*
     * Loading helpers
     */

    protected void showLoading(int message) {
        ProgressDialogFragment.builder(getFragmentManager())
                .setLoadingMessage(message)
                .show();
    }

    protected void hideLoading() {
        ProgressDialogFragment.dismiss(getFragmentManager());
    }

    /*
     * AdhocDataViewModelListener interface
     */

    @Override
    public void onPreparingStart() {
        showLoading(R.string.adv_preparing);
    }

    @Override
    public void onPreparingEnd() {
        hideLoading();
        new Handler(getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                model.run();
            }
        });
    }

    @Override
    public void onPreparingFailed(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG)
                .show();
        hideLoading();
    }

    @Override
    public void onOperationStart() {
        showLoading(R.string.adv_executing);
    }

    @Override
    public void onOperationEnd() {
        hideLoading();
    }

    @Override
    public void onOperationFailed(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG)
                .show();
        hideLoading();
    }
}
