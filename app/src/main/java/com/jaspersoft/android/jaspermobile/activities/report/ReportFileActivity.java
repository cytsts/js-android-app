/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.report;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.file.ExternalOpenFragment_;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportFileCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.ui.view.activity.ToolbarActivity;
import com.jaspersoft.android.sdk.client.oxm.resource.FileLookup;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ReportFileActivity extends ToolbarActivity {
    public static final String REPORT_URI_ARG = "reportURI";
    public static final String FILE_FORMAT_ARG = "fileFormat";

    @Inject
    protected GetReportFileCase getReportFileCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        String reportUri = getReportUri();
        showFileTitle(reportUri);

        getBaseActivityComponent().inject(this);

        getReportFileCase.execute(new PageRequest.Builder()
                .setUri(reportUri)
                .setFormat(getFileFormat().toUpperCase())
                .setRange("1-" + Integer.MAX_VALUE)
                .build(), new SimpleSubscriber<File>() {
            @Override
            public void onStart() {
                showProgressDialog();
            }

            @Override
            public void onError(Throwable e) {
                onDownloadFailed(e);
            }

            @Override
            public void onNext(File item) {
                onDownloadComplete(item.getAbsolutePath());
            }

            @Override
            public void onCompleted() {
                hideProgressDialog();
            }
        });
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_rvs_f);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        getReportFileCase.unsubscribe();
    }

    private void showProgressDialog() {
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                onBackPressed();
                            }
                        }
                )
                .show();
    }

    private void hideProgressDialog() {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    private void showFileTitle(String resourceName) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && resourceName != null) {
            actionBar.setTitle(resourceName);
        }
    }

    @NotNull
    private String getReportUri() {
        Bundle extras = getIntent().getExtras();
        String reportUri = null;
        if (extras != null) {
            reportUri = extras.getString(REPORT_URI_ARG);
        }
        if (reportUri == null) {
            throw new RuntimeException("Report uri should be provided");
        }
        return reportUri;
    }

    @NotNull
    private String getFileFormat() {
        Bundle extras = getIntent().getExtras();
        String fileFormat = null;
        if (extras != null) {
            fileFormat = extras.getString(FILE_FORMAT_ARG);
        }
        if (fileFormat == null) {
            throw new RuntimeException("File format should be provided");
        }
        return fileFormat;
    }

    public void onDownloadComplete(String fileUri) {
        FileLookup.FileType fileType = FileLookup.FileType.valueOf(getFileFormat());
        Fragment fileFragment = ExternalOpenFragment_.builder()
                .file(fileUri)
                .fileType(fileType)
                .build();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frame, fileFragment)
                .commit();

        hideProgressDialog();
    }

    public void onDownloadFailed(Throwable throwable) {
        RequestExceptionHandler.showCommonErrorMessage(ReportFileActivity.this, throwable);
        hideProgressDialog();
    }
}
