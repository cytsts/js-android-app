/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity_;
import com.jaspersoft.android.jaspermobile.activities.save.SaveDashboardActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.webresource.WebResourceActivity;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.DestinationMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.dashboard.GetDashboardControlsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.dashboard.GetDashboardVisualizeParamsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.FlushInputControlsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportMetadataCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.resource.GetResourceDetailsByTypeCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.util.InputControlHolder;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener_;
import com.jaspersoft.android.jaspermobile.webview.WebInterface;
import com.jaspersoft.android.jaspermobile.webview.WebViewEnvironment;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.AmberTwoDashboardExecutor;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardCallback;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardExecutor;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardTrigger;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardWebInterface;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.JsDashboardTrigger;
import com.jaspersoft.android.jaspermobile.webview.hyperlinks.HyperlinksCallback;
import com.jaspersoft.android.sdk.client.oxm.report.ReportDestination;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.jaspersoft.android.sdk.widget.report.renderer.RunOptions;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@OptionsMenu({R.menu.report_filter_manager_menu, R.menu.save_item_menu})
@EActivity
public class Amber2DashboardActivity extends BaseDashboardActivity implements DashboardCallback, HyperlinksCallback {

    private static final int REQUEST_DASHBOARDS_PARAMETERS = 200;

    @Inject
    GetResourceDetailsByTypeCase getResourceDetailsByTypeCase;
    @Inject
    ReportParamsMapper paramsMapper;
    @Inject
    DestinationMapper destinationMapper;
    @Inject
    ReportParamsStorage reportParamsStorage;
    @Inject
    RequestExceptionHandler requestExceptionHandler;

    @Inject
    GetDashboardControlsCase mGetDashboardControlsCase;
    @Inject
    FlushInputControlsCase mFlushInputControlsCase;
    @Inject
    GetDashboardVisualizeParamsCase mGetDashboardVisualizeParamsCase;
    @Inject
    GetReportMetadataCase mGetReportMetadataCase;
    @Inject
    RequestExceptionHandler mExceptionHandler;

    private boolean mFavoriteItemVisible, mInfoItemVisible, mFiltersVisible;
    private MenuItem favoriteAction, aboutAction, filerAction;
    private DashboardTrigger mDashboardTrigger;
    private WebInterface mWebInterface;
    private DashboardExecutor mDashboardExecutor;

    private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            Amber2DashboardActivity.super.onBackPressed();
        }
    };

    private ResourceOpener resourceOpener;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);

        resourceOpener = ResourceOpener_.getInstance_(this);

        mGetDashboardControlsCase.execute(
                resource.getUri(),
                new GenericSubscriber<>(
                        new SimpleSubscriber<Boolean>() {
                            @Override
                            public void onError(Throwable e) {
                                mFiltersVisible = false;
                            }

                            @Override
                            public void onNext(Boolean hasControls) {
                                mFiltersVisible = hasControls;
                                invalidateOptionsMenu();
                            }
                        },
                        false
                )
        );
        showMenuItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        favoriteAction = menu.findItem(R.id.favoriteAction);
        aboutAction = menu.findItem(R.id.aboutAction);
        filerAction = menu.findItem(R.id.showFilters);
        return result;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        favoriteAction.setVisible(mFavoriteItemVisible);
        aboutAction.setVisible(mInfoItemVisible);
        filerAction.setVisible(mFiltersVisible);

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.showFilters) {
            InputControlsActivity_.intent(Amber2DashboardActivity.this)
                    .reportUri(resource.getUri())
                    .dashboardInputControl(true)
                    .startForResult(REQUEST_DASHBOARDS_PARAMETERS);
            return true;
        } else if (item.getItemId() == R.id.saveAction) {
            if (FileUtils.isExternalStorageWritable()) {

                SaveDashboardActivity_.intent(this)
                        .resource(resource)
                        .start();
            } else {
                Toast.makeText(this,
                        R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @OnActivityResult(REQUEST_DASHBOARDS_PARAMETERS)
    final void onNewParametersResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            boolean isNewParamsEqualOld = data.getBooleanExtra(
                    InputControlsActivity.RESULT_SAME_PARAMS, false);
            if (!isNewParamsEqualOld) {
                applyParams();
            }
        }
    }

    @Override
    protected void onPause() {
        if (mWebInterface != null) {
            mWebInterface.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebInterface != null) {
            mWebInterface.resume();
        }
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_dvs_v);
    }

    @Override
    public void finish() {
        mGetDashboardControlsCase.unsubscribe();
        mGetDashboardVisualizeParamsCase.unsubscribe();
        mGetReportMetadataCase.unsubscribe();
        mFlushInputControlsCase.execute(resource.getUri());
        super.finish();
    }

    //---------------------------------------------------------------------
    // Abstract methods implementations
    //---------------------------------------------------------------------

    @Override
    public void onWebViewConfigured(WebView webView) {
        Log.d("Adhoc2DashboardActivity", "onWebViewConfigured");
        mDashboardTrigger = JsDashboardTrigger.with(webView);
        mDashboardExecutor = AmberTwoDashboardExecutor.newInstance(webView, mServer, resource);
        mWebInterface = DashboardWebInterface.from(this, this);
        WebViewEnvironment.configure(webView)
                .withWebInterface(mWebInterface);
        loadFlow();
    }

    @Override
    public void onPageStarted() {
        Log.d("Adhoc2DashboardActivity", "onPageStarted");
        showLoading();
    }

    @Override
    public void onPageFinished() {
        Log.d("Adhoc2DashboardActivity", "onPageFinished");
        hideLoading();
    }

    @Override
    public void onRefresh() {
        mDashboardTrigger.refreshDashboard();
    }

    @Override
    public void onHomeAsUpCalled() {
        super.onBackPressed();
    }

    @Override
    public void onSessionRefreshed() {
        loadFlow();
    }

    //---------------------------------------------------------------------
    // DashboardCallback
    //---------------------------------------------------------------------

    @UiThread
    @Override
    public void onScriptLoaded() {
        Log.d("Adhoc2DashboardActivity", "onScriptLoaded");
        showLoading();
        runDashboard();
    }

    @UiThread
    @Override
    public void onLoadStart() {
        Log.d("Adhoc2DashboardActivity", "onLoadStart");
    }

    @UiThread
    @Override
    public void onLoadDone() {
        Log.d("Adhoc2DashboardActivity", "onLoadDone");
        showWebView(true);
        hideLoading();
    }

    @UiThread
    @Override
    public void onLoadError(String error) {
        Log.d("Adhoc2DashboardActivity", "onLoadError");
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        hideLoading();
    }

    @UiThread
    @Override
    public void onWindowResizeStart() {
    }

    @UiThread
    @Override
    public void onWindowResizeEnd() {
    }

    @UiThread
    @Override
    public void onAuthError(String message) {
        scrollableTitleHelper.injectTitle(resource.getLabel());
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        super.onSessionExpired();
    }

    @UiThread
    @Override
    public void onWindowError(String errorMessage) {
        showMessage(getString(R.string.failed_load_data));
        hideLoading();
    }

    @UiThread
    @Override
    public void onReferenceClick(String href) {
        Intent i = WebResourceActivity.newIntent(this, Uri.parse(href));
        startActivity(i);
    }

    //---------------------------------------------------------------------
    // Hyperlinks
    //---------------------------------------------------------------------

    @UiThread
    @Override
    public void onReportExecutionClick(String data) {
        mGetReportMetadataCase.execute(data, new GenericSubscriber<>(new SimpleSubscriber<ResourceLookup>() {
            @Override
            public void onNext(ResourceLookup lookup) {
                resourceOpener.runReport(lookup, null);
            }
        }));
    }

    @UiThread
    @Override
    public void onRemotePageClick(String location) {
        String url = mServer.getBaseUrl() + location;
        showExternalLink(this, url);
    }

    @UiThread
    @Override
    public void onRemoteAnchorClick(String location) {
        String url = mServer.getBaseUrl() + location;
        showExternalLink(this, url);
    }

    //---------------------------------------------------------------------
    // Hyperlinks Helpers
    //---------------------------------------------------------------------

    private void showExternalLink(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // show notification if no app available to open selected format
            Toast.makeText(context,
                    context.getString(R.string.sdr_t_no_app_available, "view"),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void loadFlow() {
        mDashboardExecutor.prepare();
    }

    private void runDashboard() {
        mDashboardExecutor.execute();
    }

    private void applyParams() {
        mGetDashboardVisualizeParamsCase.execute(resource.getUri(), new GenericSubscriber<>(new SimpleSubscriber<String>() {
            @Override
            public void onError(Throwable e) {
                String message = RequestExceptionHandler.extractMessage(Amber2DashboardActivity.this, e);
                showMessage(message);
            }

            @Override
            public void onNext(String params) {
                mDashboardTrigger.applyParams(params);
            }
        }));
    }

    private void showMenuItems() {
        mFavoriteItemVisible = mInfoItemVisible = true;
        supportInvalidateOptionsMenu();
    }

    private void hideMenuItems() {
        mFavoriteItemVisible = mInfoItemVisible = false;
        supportInvalidateOptionsMenu();
    }

    /*
     *  GetResourceDetailListener
     */

    private class GetResourceDetailListener extends SimpleSubscriber<ResourceLookup> {
        private final RunOptions runOptions;

        private GetResourceDetailListener(RunOptions runOptions) {
            this.runOptions = runOptions;
        }

        public void onStart() {
            ProgressDialogFragment.builder(getSupportFragmentManager())
                    .setLoadingMessage(R.string.loading_msg)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            getResourceDetailsByTypeCase.unsubscribe();
                        }
                    })
                    .show();
        }

        @Override
        public void onError(Throwable e) {
            requestExceptionHandler.showAuthErrorIfExists(e);
        }

        @Override
        public void onNext(ResourceLookup item) {
            List<ReportParameter> reportParams = runOptions.getParameters();
            List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> legacyReportParams = paramsMapper.retrofittedParamsToLegacy(reportParams);
            ReportDestination reportDestination = destinationMapper.toReportDestination(runOptions.getDestination());

            InputControlHolder icHolder = reportParamsStorage.getInputControlHolder(item.getUri());
            icHolder.setReportParams(legacyReportParams);

            resourceOpener.runReport(item, reportDestination);
        }

        @Override
        public void onCompleted() {
            ProgressDialogFragment.dismiss(getSupportFragmentManager());
        }
    }
}
