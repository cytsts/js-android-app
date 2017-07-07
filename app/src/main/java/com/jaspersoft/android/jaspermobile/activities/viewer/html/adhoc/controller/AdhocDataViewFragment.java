package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.chartTypes.ChartTypesActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller.AdhocDataViewModel.Event;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller.AdhocDataViewModel.Operation;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.filters.FiltersActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.filters.FiltersFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.AdhocDataViewModelImpl;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.VisualizeWebEnvironment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.internal.di.components.AdhocDataViewFragmentComponent;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.widget.report.renderer.ChartType;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;


/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public class AdhocDataViewFragment extends Fragment implements AdhocDataViewModel.OperationListener, AdhocDataViewModel.EventListener {

    @Inject
    JasperServer mServer;

    static final String ARG_RESOURCE_LOOKUP = "resource_lookup";
    private static final int SELECTED_CANVAS_TYPE_CODE = 102;
    private static final int REQUEST_ADHOC_PARAMETERS = 103;

    private AdhocDataViewModelImpl model;
    VisualizeWebEnvironment webEnvironment;

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
        getComponent().inject(this);
        webEnvironment = new VisualizeWebEnvironment(getContext().getApplicationContext(), mServer.getBaseUrl());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ResourceLookup resourceLookup = getArguments().getParcelable(ARG_RESOURCE_LOOKUP);
        assert resourceLookup != null;
        model = new AdhocDataViewModelImpl(webEnvironment, resourceLookup);

        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.fragment_adhoc_data_view, container, false);
        v.addView(webEnvironment.getWebView(), 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(resourceLookup.getLabel());
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.adhoc_data_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshAction: {
                model.refresh();
                return true;
            }
            case R.id.changeCanvasTypeAction: {
                model.askAvailableChartTypes();
                return true;
            }
            case R.id.filtersAction: {
                showFilters();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == SELECTED_CANVAS_TYPE_CODE) {
            ChartType chartType = data.getExtras().getParcelable(ChartTypesActivity.SELECTED_CHART_TYPE_ARG);
            if (chartType == null) {
                throw new RuntimeException("Selected chartType should be provided");
            }
            model.changeCanvasType(chartType);
        } else if (requestCode == REQUEST_ADHOC_PARAMETERS) {
            // apply params
            Boolean isParamsChanged = !data.getExtras().getBoolean(FiltersFragment.RESULT_SAME_PARAMS);
            if (isParamsChanged) {
                model.applyFilters();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        model.subscribeOperationListener(this);
        model.subscribeEventListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        model.unsubscribeOperationListener(this);
        model.unsubscribeEventListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        model.destroy();
    }

    private AdhocDataViewFragmentComponent getComponent() {
        return GraphObject.Factory.from(getContext())
                .getProfileComponent()
                .plusAdhocDataViewPage();
    }

    /*
     * Private
     */

    private void showAvailableCanvasTypes(List<ChartType> canvasTypes, ChartType currentCanvasType) {
        Intent chartTypesIntent = new Intent(this.getContext(), ChartTypesActivity.class);

        chartTypesIntent.putParcelableArrayListExtra(ChartTypesActivity.CHART_TYPES_ARG, new ArrayList<>(canvasTypes));
        chartTypesIntent.putExtra(ChartTypesActivity.SELECTED_CHART_TYPE_ARG, currentCanvasType);

        startActivityForResult(chartTypesIntent, SELECTED_CANVAS_TYPE_CODE);
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
     * AdhocDataViewModel.EventListener
     */

    @Override
    public void onEventReceived(Event event) {
        switch (event.getType()) {
            case ENVIRONMENT_PREPARING:
                webEnvironment.getWebView().setVisibility(View.INVISIBLE);
                showLoading(R.string.adv_preparing);
                break;
            case ENVIRONMENT_READY:
                webEnvironment.getWebView().setVisibility(View.VISIBLE);
                hideLoading();
                model.run();
                break;
            case ERROR:
                webEnvironment.getWebView().setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), (String)event.getData(), Toast.LENGTH_LONG)
                        .show();
                hideLoading();
                break;
        }
    }

    /*
     * AdhocDataViewModel.OperationListener
     */

    @Override
    public void onOperationStart(Operation operation) {
        webEnvironment.getWebView().setVisibility(View.INVISIBLE);
        showLoading(R.string.adv_executing);
    }

    @Override
    public void onOperationEnd(Operation operation) {
        webEnvironment.getWebView().setVisibility(View.VISIBLE);
        hideLoading();
        switch (operation) {
            case ASK_AVAILABLE_CANVAS_TYPES:
                showAvailableCanvasTypes(model.getCanvasTypes(), model.getCurrentCanvasType());
                break;
        }
    }

    @Override
    public void onOperationFailed(Operation operation, String error) {
        webEnvironment.getWebView().setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG)
                .show();
        hideLoading();
    }

    /*
     * Work with filters
     */

    public void showFilters() {
        Intent i = FiltersActivity.newIntent(getActivity(), model.getResourceLookup().getUri());
        startActivityForResult(i, REQUEST_ADHOC_PARAMETERS);
    }
}
