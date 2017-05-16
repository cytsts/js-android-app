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

package com.jaspersoft.android.jaspermobile.activities.save;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.ExportBundle;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.ComponentProviderDelegate;
import com.jaspersoft.android.jaspermobile.util.SavedItemHelper;
import com.jaspersoft.android.jaspermobile.util.SavedItemHelper_;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.dashboard.DashboardService;
import com.jaspersoft.android.sdk.service.data.dashboard.DashboardControlComponent;
import com.jaspersoft.android.sdk.service.data.dashboard.DashboardExportFormat;
import com.jaspersoft.android.sdk.service.data.dashboard.DashboardExportOptions;
import com.jaspersoft.android.sdk.service.data.report.PageRange;
import com.jaspersoft.android.sdk.service.data.report.ReportExportOutput;
import com.jaspersoft.android.sdk.service.data.report.ResourceOutput;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.report.ReportAttachment;
import com.jaspersoft.android.sdk.service.report.ReportExecution;
import com.jaspersoft.android.sdk.service.report.ReportExecutionOptions;
import com.jaspersoft.android.sdk.service.report.ReportExport;
import com.jaspersoft.android.sdk.service.report.ReportExportOptions;
import com.jaspersoft.android.sdk.service.report.ReportFormat;
import com.jaspersoft.android.sdk.service.report.ReportService;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ResourceDownloadManager {

    @Inject
    protected Profile mProfile;
    @Inject
    protected JasperRestClient mRestClient;
    @Inject
    protected ReportParamsCache mReportParamsCache;
    @Inject
    protected ReportParamsMapper mReportParamsMapper;
    @Inject
    protected ControlsRepository mControlsRepository;

    private SavedItemHelper mSavedItemHelper;

    private Context mContext;
    private HashMap<Uri, AsyncTask> mDownloadsPool;
    private ResourceDownloadCallback listener;

    public ResourceDownloadManager(Context context) {
        ComponentProviderDelegate.INSTANCE
                .getProfileComponent(context)
                .inject(this);

        mContext = context;
        mDownloadsPool = new HashMap<>();
        mSavedItemHelper = SavedItemHelper_.getInstance_(context);
        listener = ResourceDownloadCallback.EMPTY;
    }

    public void downloadResource(ExportBundle exportBundle, ResourceLookup.ResourceType resourceType) {
        Uri resourceUri = addSavedItemRecord(exportBundle, resourceType);
        SaveResourceAsyncTask asyncTask;
        if (resourceType == ResourceLookup.ResourceType.reportUnit) {
            asyncTask = new SaveReportAsyncTask(resourceUri);
        } else {
            asyncTask = new SaveDashboardAsyncTask(resourceUri);
        }
        mDownloadsPool.put(resourceUri, asyncTask);

        AsyncTaskCompat.executeParallel(asyncTask, exportBundle);
    }

    public void cancelDownloading(Uri reportUri) {
        mDownloadsPool.get(reportUri).cancel(true);
    }

    public void setResourceDownloadCallback(ResourceDownloadCallback listener) {
        if (listener != null) {
            this.listener = listener;
        }
    }

    private Uri addSavedItemRecord(ExportBundle bundle, ResourceLookup.ResourceType resourceType) {
        String descriptionExtra = bundle.getDescription();
        String outputFormatExtra = bundle.getFormat();
        File resourceFileExtra = bundle.getFile();
        String savedResourceNameExtra = bundle.getLabel();

        SavedItems savedItemsEntry = new SavedItems();
        savedItemsEntry.setName(savedResourceNameExtra);
        savedItemsEntry.setFilePath(resourceFileExtra.getPath());
        savedItemsEntry.setFileFormat(outputFormatExtra);
        savedItemsEntry.setDescription(descriptionExtra);
        savedItemsEntry.setWstype(resourceType.toString());
        savedItemsEntry.setCreationTime(new Date().getTime());
        savedItemsEntry.setAccountName(mProfile.getKey());
        savedItemsEntry.setDownloaded(false);

        return mContext.getContentResolver().insert(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                savedItemsEntry.getContentValues());
    }

    private boolean updateSavedItemRecord(Uri recordUri) {
        SavedItems savedItemsEntry = new SavedItems();
        savedItemsEntry.setDownloaded(true);

        return mContext.getContentResolver().update(recordUri, savedItemsEntry.getContentValues(), null, null) == 1;
    }

    private void removeUnsavedItem(Uri recordUri) {
        mSavedItemHelper.deleteSavedItem(recordUri);
    }

    private void saveReport(ExportBundle bundle) throws ServiceException, IOException {
        String reportUri = bundle.getUri();
        String format = bundle.getFormat();
        String range = bundle.getPageRange();
        File file = bundle.getFile();

        ReportFormat reportFormat = ReportFormat.valueOf(format);

        PageRange pageRange = null;
        if (range != null) {
            pageRange = PageRange.parse(range);
        }

        List<ReportParameter> parameters = mReportParamsCache.get(reportUri);
        List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> params =
                mReportParamsMapper.legacyParamsToRetrofitted(parameters);

        ReportService reportService = mRestClient.syncReportService();
        ReportExecutionOptions execOptions = ReportExecutionOptions.builder()
                .withInteractive(false)
                .withFormat(reportFormat)
                .withAttachmentPrefix("./")
                .withPageRange(pageRange)
                .withParams(params)
                .build();
        ReportExecution run = reportService.run(reportUri, execOptions);

        ReportExportOptions exportOptions = ReportExportOptions.builder()
                .withAttachmentPrefix("./")
                .withFormat(reportFormat)
                .withPageRange(pageRange)
                .build();
        ReportExport export = run.export(exportOptions);
        ReportExportOutput output = export.download();

        FileUtils.copyInputStreamToFile(output.getStream(), file);
        if ("HTML".equals(format)) {
            List<ReportAttachment> attachments = export.getAttachments();

            for (ReportAttachment attachment : attachments) {
                File attachmentFile = new File(file.getParentFile(), attachment.getFileName());
                ResourceOutput resourceOutput = attachment.download();
                FileUtils.copyInputStreamToFile(resourceOutput.getStream(), attachmentFile);
            }
        }
    }

    private void saveDashboard(ExportBundle bundle) throws ServiceException, IOException {
        String dashboardUri = bundle.getUri();
        String format = bundle.getFormat();
        File file = bundle.getFile();

        DashboardExportFormat dashboardExportFormat = DashboardExportFormat.valueOf(format);

        List<ReportParameter> reportParameters = mReportParamsCache.get(dashboardUri);
        List<DashboardControlComponent> dashboardControlComponent = mControlsRepository.listDashboardControlComponents(dashboardUri).toBlocking().first();
        List<ReportParameter> dashboardParams = mReportParamsMapper.adaptDashboardControlComponents(reportParameters, dashboardControlComponent);
        List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> params =
                mReportParamsMapper.legacyParamsToRetrofitted(dashboardParams);

        DashboardService dashboardService = mRestClient.syncDashboardService();

        DashboardExportOptions dashboardExportOptions = new DashboardExportOptions.Builder(dashboardUri, dashboardExportFormat)
                .setParameters(params)
                .build();

        ResourceOutput resourceOutput = dashboardService.export(dashboardExportOptions);
        FileUtils.copyInputStreamToFile(resourceOutput.getStream(), file);
    }

    public interface ResourceDownloadCallback {
        ResourceDownloadCallback EMPTY = new ResourceDownloadCallback() {
            @Override
            public void onDownloadCountChange(int count) {
            }

            @Override
            public void onDownloadComplete(String resourceName) {
            }

            @Override
            public void onDownloadFailed(String resourceName) {
            }

            @Override
            public void onDownloadCanceled() {

            }
        };

        void onDownloadCountChange(int count);

        void onDownloadComplete(String resourceName);

        void onDownloadFailed(String resourceName);

        void onDownloadCanceled();
    }

    private abstract class SaveResourceAsyncTask extends AsyncTask<ExportBundle, Void, Boolean> {
        private Uri mResourceUri;
        private String mResourceName;

        public SaveResourceAsyncTask(Uri resourceUri) {
            this.mResourceUri = resourceUri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            listener.onDownloadCountChange(mDownloadsPool.size());
        }

        @Override
        protected Boolean doInBackground(ExportBundle... params) {
            ExportBundle exportBundle = params[0];
            mResourceName = exportBundle.getLabel();

            try {
                saveResource(exportBundle);
                return true;
            } catch (ServiceException | IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            mDownloadsPool.remove(mResourceUri);
            listener.onDownloadCountChange(mDownloadsPool.size());

            if (success && updateSavedItemRecord(mResourceUri)) {
                listener.onDownloadComplete(mResourceName);
            } else {
                removeUnsavedItem(mResourceUri);
                listener.onDownloadFailed(mResourceName);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            removeUnsavedItem(mResourceUri);
            mDownloadsPool.remove(mResourceUri);

            listener.onDownloadCanceled();
            listener.onDownloadCountChange(mDownloadsPool.size());
        }

        protected abstract void saveResource(ExportBundle exportBundle) throws ServiceException, IOException;
    }

    private class SaveReportAsyncTask extends SaveResourceAsyncTask {
        public SaveReportAsyncTask(Uri reportUri) {
            super(reportUri);
        }

        @Override
        protected void saveResource(ExportBundle exportBundle) throws ServiceException, IOException {
            saveReport(exportBundle);
        }
    }

    private class SaveDashboardAsyncTask extends SaveResourceAsyncTask {
        public SaveDashboardAsyncTask(Uri reportUri) {
            super(reportUri);
        }

        @Override
        protected void saveResource(ExportBundle exportBundle) throws ServiceException, IOException {
            saveDashboard(exportBundle);
        }
    }
}
