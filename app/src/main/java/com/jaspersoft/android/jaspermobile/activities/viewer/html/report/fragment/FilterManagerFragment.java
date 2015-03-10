package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.ReportOptionsActivity;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener2;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import java.util.ArrayList;

import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.EXTRA_REPORT_CONTROLS;
import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.EXTRA_REPORT_PARAMETERS;
import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.REQUEST_REPORT_PARAMETERS;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.report_filter_manager_menu)
public class FilterManagerFragment extends RoboSpiceFragment {
    public static final String TAG = FilterManagerFragment.class.getSimpleName();

    @Inject
    JsRestClient jsRestClient;

    @FragmentArg
    ResourceLookup resource;

    @OptionsMenuItem
    MenuItem saveReport;
    @OptionsMenuItem
    MenuItem showFilters;

    @InstanceState
    ArrayList<InputControl> cachedInputControls;
    @InstanceState
    ArrayList<ReportParameter> reportParameters;
    @InstanceState
    ArrayList<InputControl> validInputControls;
    @InstanceState
    ArrayList<ReportParameter> validReportParameters;
    @InstanceState
    boolean mShowFilterOption;
    @InstanceState
    boolean mShowSaveOption;

    private ReportExecutionFragment reportExecutionFragment;
    private RequestExecutor requestExecutor;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        requestExecutor = RequestExecutor.builder()
                .setExecutionMode(RequestExecutor.Mode.VISIBLE)
                .setFragmentManager(getFragmentManager())
                .setSpiceManager(getSpiceManager())
                .create();

        final GetInputControlsRequest request =
                new GetInputControlsRequest(jsRestClient, resource.getUri());
        requestExecutor.execute(request, new GetInputControlsListener());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        saveReport.setVisible(mShowSaveOption);
        showFilters.setVisible(mShowFilterOption);
    }

    @OptionsItem
    final void saveReport() {
        if (FileUtils.isExternalStorageWritable()) {
            PaginationManagerFragment manager = (PaginationManagerFragment) getFragmentManager().findFragmentByTag(PaginationManagerFragment.TAG);

            SaveReportActivity_.intent(this)
                    .requestId(getReportExecutionFragment().getRequestId())
                    .reportParameters(reportParameters)
                    .resource(resource)
                    .pageCount(manager.mTotalPage)
                    .start();
        } else {
            Toast.makeText(getActivity(),
                    R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    @OptionsItem
    public void showFilters() {
        showReportOptions(cachedInputControls);
    }

    public void showPreviousReport() {
        reportParameters = validReportParameters;
        cachedInputControls = validInputControls;
        getReportExecutionFragment().executeReport(reportParameters);
    }

    public boolean hasSnapshot() {
        return validInputControls != null && validReportParameters != null;
    }

    public void makeSnapshot() {
        validReportParameters = reportParameters;
        validInputControls = cachedInputControls;
    }

    private void showReportOptions(ArrayList<InputControl> inputControls) {
        // Run Report Options activity
        Intent intent = new Intent(getActivity(), ReportOptionsActivity.class);
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_URI, resource.getUri());
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_LABEL, resource.getLabel());
        intent.putParcelableArrayListExtra(ReportOptionsActivity.EXTRA_REPORT_CONTROLS, inputControls);
        startActivityForResult(intent, REQUEST_REPORT_PARAMETERS);
    }

    @OnActivityResult(REQUEST_REPORT_PARAMETERS)
    final void loadReportParameters(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            reportParameters = data.getParcelableArrayListExtra(EXTRA_REPORT_PARAMETERS);
            cachedInputControls = data.getParcelableArrayListExtra(EXTRA_REPORT_CONTROLS);

            getReportExecutionFragment().executeReport(reportParameters);
        } else {
            // Check if user has experienced report loading. Otherwise remove him from this page.
            if (!hasSnapshot()) {
                getActivity().finish();
            } else {
                showPreviousReport();
            }
        }
    }

    private ReportExecutionFragment getReportExecutionFragment() {
        if (reportExecutionFragment == null) {
            reportExecutionFragment = (ReportExecutionFragment)
                    getFragmentManager().findFragmentByTag(ReportExecutionFragment.TAG);
        }
        return reportExecutionFragment;
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class GetInputControlsListener extends SimpleRequestListener2<InputControlsList> {

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            super.onRequestFailure(exception);
            ProgressDialogFragment.dismiss(getFragmentManager());
        }

        @Override
        public void onRequestSuccess(InputControlsList controlsList) {
            ArrayList<InputControl> inputControls = new ArrayList<InputControl>(controlsList.getInputControls());
            boolean showFilterActionVisible = !inputControls.isEmpty();
            mShowFilterOption = showFilterActionVisible;
            mShowSaveOption = true;
            getActivity().invalidateOptionsMenu();

            if (showFilterActionVisible) {
                cachedInputControls = inputControls;
                showReportOptions(inputControls);
                ProgressDialogFragment.dismiss(getFragmentManager());
            } else {
                getReportExecutionFragment().executeReport();
            }
        }
    }

}