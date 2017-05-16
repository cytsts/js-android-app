/*
 * Copyright � 2016 TIBCO Software,Inc.All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.save;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.data.entity.ExportBundle;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.ComponentProviderDelegate;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;

import java.util.Date;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */

@EService
public class SaveResourceService extends Service implements ResourceDownloadManager.ResourceDownloadCallback {

    public final static String ACTION_SAVE_RESOURCE = "saveResource";
    public final static String ACTION_CANCEL_SAVING = "cancelSaving";

    public final static String EXPORT_BUNDLE_EXTRA = "exportBundle";
    public final static String RESOURCE_FORMAT_EXTRA = "resourceFormat";
    public final static String ITEM_URI_EXTRA = "itemUri";

    private static final int LOADING_NOTIFICATION_ID = 434;

    @Inject
    protected Analytics analytics;

    @SystemService
    protected NotificationManager mNotificationManager;
    private ResourceDownloadManager mResourceDownloadManager;

    public static void start(Context context, ExportBundle bundle, ResourceLookup.ResourceType resourceType) {
        Intent startIntent = SaveResourceService_.intent(context).get();
        startIntent.setAction(ACTION_SAVE_RESOURCE);
        startIntent.putExtra(EXPORT_BUNDLE_EXTRA, bundle);
        startIntent.putExtra(RESOURCE_FORMAT_EXTRA, resourceType);
        context.startService(startIntent);
    }

    public static void cancel(Context context, Uri resourceUri) {
        Intent cancelIntent = SaveResourceService_.intent(context).get();
        cancelIntent.setAction(ACTION_CANCEL_SAVING);
        cancelIntent.putExtra(ITEM_URI_EXTRA, resourceUri);
        context.startService(cancelIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ComponentProviderDelegate.INSTANCE
                .getProfileComponent(this)
                .inject(this);

        mResourceDownloadManager = new ResourceDownloadManager(this);
        mResourceDownloadManager.setResourceDownloadCallback(this);

        startForegroundNotification();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (SaveResourceService.ACTION_CANCEL_SAVING.equals(action)) {
            Uri resourceUri = extras.getParcelable(SaveResourceService.ITEM_URI_EXTRA);
            mResourceDownloadManager.cancelDownloading(resourceUri);
        } else {
            ExportBundle bundle = extras.getParcelable(SaveResourceService.EXPORT_BUNDLE_EXTRA);
            ResourceLookup.ResourceType resourceType = (ResourceLookup.ResourceType) extras.getSerializable(SaveResourceService.RESOURCE_FORMAT_EXTRA);
            mResourceDownloadManager.downloadResource(bundle, resourceType);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDownloadCountChange(int count) {
        if (count > 0) {
            updateDownloadingNotification(count);
        } else{
            stopSelf();
        }
    }

    @Override
    public void onDownloadComplete(String sourceName) {
        notifySaveResult(sourceName, android.R.drawable.stat_sys_download_done, getString(R.string.sr_t_report_saved));
        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.SAVED.getValue(), Analytics.EventLabel.DONE.getValue());
    }

    @Override
    public void onDownloadFailed(String resourceName) {
        notifySaveResult(resourceName, android.R.drawable.ic_dialog_alert, getString(R.string.sdr_saving_error_msg));
        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.SAVED.getValue(), Analytics.EventLabel.FAILED.getValue());
    }

    @Override
    public void onDownloadCanceled() {
        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.SAVED.getValue(), Analytics.EventLabel.CANCELED.getValue());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cancelForegroundNotification();
    }

    private void startForegroundNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(getString(R.string.sdr_starting_downloading_msg));
        startForeground(LOADING_NOTIFICATION_ID, mBuilder.build());
    }

    private void cancelForegroundNotification() {
        mNotificationManager.cancel(LOADING_NOTIFICATION_ID);
    }

    private void updateDownloadingNotification(int downloadingCount) {
        String savingTitle = downloadingCount > 1 ? getString(R.string.sdr_saving_multiply_msg, downloadingCount) : getString(R.string.sdr_saving_msg);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(savingTitle)
                .setContentIntent(getSavedItemIntent());

        mNotificationManager.notify(LOADING_NOTIFICATION_ID, mBuilder.build());
    }

    private void notifySaveResult(String resourceName, int iconId, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(iconId)
                .setContentTitle(resourceName)
                .setContentText(message)
                .setContentIntent(getSavedItemIntent())
                .setAutoCancel(true);

        mNotificationManager.notify(createNotificationId(), mBuilder.build());
    }

    private int createNotificationId() {
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last5Str = tmpStr.substring(tmpStr.length() - 6);
        return Integer.valueOf(last5Str);
    }

    private PendingIntent getSavedItemIntent() {
        Intent notificationIntent = NavigationActivity_.intent(this)
                .currentSelection(R.id.vg_saved_items)
                .get();

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
