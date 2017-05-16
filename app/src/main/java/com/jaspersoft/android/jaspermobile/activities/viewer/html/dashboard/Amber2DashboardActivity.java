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
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity_;
import com.jaspersoft.android.jaspermobile.activities.save.SaveDashboardActivity_;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.dashboard.GetDashboardControlsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.dashboard.GetDashboardVisualizeParamsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.FlushInputControlsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportMetadataCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.ui.model.visualize.ExecutionReferenceClickEvent;
import com.jaspersoft.android.jaspermobile.ui.model.visualize.ExternalReferenceClickEvent;
import com.jaspersoft.android.jaspermobile.ui.view.activity.ReportVisualizeActivity_;
import com.jaspersoft.android.jaspermobile.webview.WebInterface;
import com.jaspersoft.android.jaspermobile.webview.WebViewEnvironment;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.AmberTwoDashboardExecutor;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardCallback;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardExecutor;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardTrigger;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardWebInterface;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.JsDashboardTrigger;
import com.jaspersoft.android.jaspermobile.webview.hyperlinks.HyperlinksCallback;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;

import javax.inject.Inject;

import rx.subjects.PublishSubject;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@OptionsMenu({R.menu.report_filter_manager_menu, R.menu.save_item_menu})
@EActivity
public class Amber2DashboardActivity extends BaseDashboardActivity implements DashboardCallback, HyperlinksCallback {

    private static final int REQUEST_DASHBOARDS_PARAMETERS = 200;

    @InstanceState
    protected boolean mMaximized;

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

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);

        mGetDashboardControlsCase.execute(resource.getUri(), new GenericSubscriber<>(new SimpleSubscriber<Boolean>() {
            @Override
            public void onError(Throwable e) {
                mFiltersVisible = false;
            }

            @Override
            public void onNext(Boolean hasControls) {
                mFiltersVisible = hasControls;
                invalidateOptionsMenu();
            }
        }));
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

    //---------------------------------------------------------------------
    // Abstract methods implementations
    //---------------------------------------------------------------------

    @Override
    public void onWebViewConfigured(WebView webView) {
        mDashboardTrigger = JsDashboardTrigger.with(webView);
        mDashboardExecutor = AmberTwoDashboardExecutor.newInstance(webView, mServer, resource);
        mWebInterface = DashboardWebInterface.from(this, this);
        WebViewEnvironment.configure(webView)
                .withWebInterface(mWebInterface);
        loadFlow();
    }

    @UiThread
    @Override
    public void onMaximizeStart(String title) {
        hideMenuItems();
        showLoading();
    }

    @UiThread
    @Override
    public void onMaximizeEnd(String title) {
        hideLoading();
        resetZoom();
        mMaximized = true;
        scrollableTitleHelper.injectTitle(title);
    }

    @UiThread
    @Override
    public void onMaximizeFailed(String error) {
        hideLoading();
    }

    @UiThread
    @Override
    public void onMinimizeStart() {
        showMenuItems();
        showLoading();
    }

    @UiThread
    @Override
    public void onMinimizeEnd() {
        hideLoading();
        resetZoom();
        mMaximized = false;
        scrollableTitleHelper.injectTitle(resource.getLabel());
    }

    @UiThread
    @Override
    public void onMinimizeFailed(String error) {
    }

    @UiThread
    @Override
    public void onScriptLoaded() {
        runDashboard();
    }

    @UiThread
    @Override
    public void onLoadStart() {
        showLoading();
    }

    @UiThread
    @Override
    public void onLoadDone() {
        showWebView(true);
        hideLoading();
    }

    @UiThread
    @Override
    public void onLoadError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        hideLoading();
    }

    @Override
    public void onWindowResizeStart() {
    }

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

    @Override
    public void onPageFinished() {
    }

    @Override
    public void onRefresh() {
        if (mMaximized) {
            mDashboardTrigger.refreshDashlet();
        } else {
            mDashboardTrigger.refreshDashboard();
        }
    }

    @Override
    public void onHomeAsUpCalled() {
        if (mMaximized && webView != null) {
            mDashboardTrigger.minimizeDashlet();
            scrollableTitleHelper.injectTitle(resource.getLabel());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSessionRefreshed() {
        loadFlow();
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
    // Hyperlinks
    //---------------------------------------------------------------------

    @UiThread
    @Override
    public void onReportExecutionClick(String data) {
        mGetReportMetadataCase.execute(data, new GenericSubscriber<>(new SimpleSubscriber<ResourceLookup>() {
            @Override
            public void onNext(ResourceLookup lookup) {
                ReportVisualizeActivity_.intent(Amber2DashboardActivity.this)
                        .resource(lookup)
                        .start();
            }
        }));
    }

    @UiThread
    @Override
    public void onReferenceClick(String location) {
        showExternalLink(this, location);
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
}
