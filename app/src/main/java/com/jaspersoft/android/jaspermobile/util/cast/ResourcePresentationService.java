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

package com.jaspersoft.android.jaspermobile.util.cast;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.BaseReportActivity;
import com.jaspersoft.android.jaspermobile.activities.report.ReportCastActivity;
import com.jaspersoft.android.jaspermobile.domain.ScreenCapture;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.ComponentProviderDelegate;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.widget.report.view.ReportView;
import com.jaspersoft.android.sdk.widget.report.view.ReportWidget;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ResourcePresentationService extends CastRemoteDisplayLocalService implements DialogInterface.OnShowListener {
    private static final ResourcePresentationCallback RESOURCE_PRESENTATION_CALLBACK = new ResourcePresentationCallback() {
        @Override
        public void onPresentationStarted() {

        }

        @Override
        public void onPresentationStopped() {

        }
    };

    private static final ResourceCastStateCallback RESOURCE_CAST_STATE_CALLBACK = new ResourceCastStateCallback() {
        @Override
        public void onCastStateChanged() {

        }
    };

    @Inject
    ReportParamsStorage reportParamsStorage;
    @Inject
    Analytics analytics;

    private static ResourcePresentationCallback resourcePresentationCallback = RESOURCE_PRESENTATION_CALLBACK;
    private static ResourceCastStateCallback resourceCastStateCallback = RESOURCE_CAST_STATE_CALLBACK;

    private ReportPresentation reportPresentation;
    private ResourceLookup currentReport;
    private String deviceName;

    @Override
    public void onCreate() {
        super.onCreate();
        ComponentProviderDelegate.INSTANCE
                .getProfileComponent(this)
                .inject(this);
    }

    @Override
    public void onCreatePresentation(Display display) {
        onDismissPresentation(true);
        reportPresentation = new ReportPresentation(this, display);

        try {
            reportPresentation.setOnShowListener(this);
            reportPresentation.show();
        } catch (WindowManager.InvalidDisplayException ex) {
            onDismissPresentation();
        }
        analytics.sendEvent(Analytics.EventCategory.CAST.getValue(), Analytics.EventAction.PRESENTED.getValue(), null);
    }

    @Override
    public void onDismissPresentation() {
        onDismissPresentation(false);
    }

    @Override
    public void onShow(DialogInterface dialog) {
        resourcePresentationCallback.onPresentationStarted();
    }

    public void setCastDeviceName(String castDeviceName) {
        deviceName = castDeviceName;
    }

    public static boolean isStarted() {
        return getInstance() != null;
    }

    public ReportWidget getReportViewer() {
        if (reportPresentation == null) return null;
        return reportPresentation.reportView;
    }

    public String getCurrentReportUri() {
        if (currentReport == null) return null;
        return currentReport.getUri();
    }

    public String getCurrentReportName() {
        if (currentReport == null) return null;
        return currentReport.getLabel();
    }

    public Bitmap getThumbnail() {
        if (reportPresentation == null || !reportPresentation.reportView.isControlActionsAvailable())
            return null;
        return ScreenCapture.Factory.captureBitmap(reportPresentation.reportView.getView());
    }

    public void setReportPresentationCallback(ResourcePresentationCallback resourcePresentationCallback) {
        ResourcePresentationService.resourcePresentationCallback = resourcePresentationCallback;
        if (ResourcePresentationService.resourcePresentationCallback == null) {
            ResourcePresentationService.resourcePresentationCallback = RESOURCE_PRESENTATION_CALLBACK;
        }
    }

    public static void setResourceCastStateCallback(ResourceCastStateCallback resourceCastStateCallback) {
        ResourcePresentationService.resourceCastStateCallback = resourceCastStateCallback;
        if (ResourcePresentationService.resourceCastStateCallback == null) {
            ResourcePresentationService.resourceCastStateCallback = RESOURCE_CAST_STATE_CALLBACK;
        }
    }

    public void stopCasting() {
        onCloseReportCasting();
        resourcePresentationCallback.onPresentationStopped();
    }

    public void onStartReportCasting(ResourceLookup report) {
        reportPresentation.reportView.setVisibility(View.VISIBLE);
        currentReport = report;
        updateCastNotification();

        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.PRESENTED.getValue(), "reportUnit");
    }

    public void onReportRenderStateUpdated() {
        resourceCastStateCallback.onCastStateChanged();
        updateCastNotification();
    }

    public void onCloseReportCasting() {
        if (reportPresentation != null) {
            reportPresentation.reportView.reset();
            reportPresentation.reportView.setVisibility(View.GONE);
        }
        if (currentReport != null) {
            reportParamsStorage.clearInputControlHolder(currentReport.getUri());
            currentReport = null;
        }
        updateCastNotification();

        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.PRESENTATION_STOPPED.getValue(), null);
    }

    private void onDismissPresentation(boolean silent) {
        if (reportPresentation != null) {
            reportPresentation.dismiss();
            reportPresentation = null;
        }
        if (!silent) {
            stopCasting();
        }
    }

    private void updateCastNotification() {
        NotificationSettings notificationSettings = new NotificationSettings.Builder()
                .setNotification(createCastNotification())
                .build();
        updateNotificationSettings(notificationSettings);
    }

    private Notification createCastNotification() {
        NotificationCompat.Builder castNotificationBuilder = new NotificationCompat.Builder(this);

        Intent intent;
        String title;

        if (currentReport == null) {
            title = getString(R.string.cast_ready_message);
            intent = NavigationActivity_.intent(this).get();
        } else {
            title = currentReport.getLabel();
            intent = new Intent(this, ReportCastActivity.class);
            intent.putExtra(BaseReportActivity.RESOURCE_LOOKUP_ARG, currentReport);
            castNotificationBuilder
                    .addAction(R.drawable.ic_menu_stop, "", PendingIntent.getBroadcast(this, 0, new Intent(getString(R.string.resource_cast_cancel_intent)), 0));


            Bitmap thumbnail = getThumbnail();
            if (thumbnail != null) {
                castNotificationBuilder.setLargeIcon(thumbnail)
                        .setStyle(new NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0));
            }
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        castNotificationBuilder.setSmallIcon(R.drawable.im_logo_single)
                .setWhen(0)
                .setContentTitle(title)
                .setContentText(deviceName)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));

        Intent stopIntent = new Intent(getString(R.string.resource_presentation_stop_intent));
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        castNotificationBuilder.addAction(R.drawable.ic_menu_close, "", broadcast);

        return castNotificationBuilder.build();
    }

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    public interface ResourcePresentationCallback {
        void onPresentationStarted();

        void onPresentationStopped();
    }

    public interface ResourceCastStateCallback {
        void onCastStateChanged();
    }

    public class ReportPresentation extends CastPresentation {
        private ReportView reportView;

        public ReportPresentation(Context serviceContext, Display display) {
            super(serviceContext, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.report_presentation);

            reportView = (ReportView) findViewById(R.id.reportView);
        }
    }
}
