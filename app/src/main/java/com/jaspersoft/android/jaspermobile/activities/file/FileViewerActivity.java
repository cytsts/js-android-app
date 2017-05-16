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

package com.jaspersoft.android.jaspermobile.activities.file;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.domain.ResourceDetailsRequest;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.resource.GetResourceDetailsByTypeCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.ui.view.activity.ToolbarActivity;
import com.jaspersoft.android.sdk.client.oxm.resource.FileLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class FileViewerActivity extends ToolbarActivity {
    public static final String RESOURCE_URI_ARG = "resourceURI";

    @Inject
    protected GetResourceDetailsByTypeCase mGetResourceDetailsByTypeCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);
        getBaseActivityComponent().inject(this);

        if (savedInstanceState == null) {
            getFileInfo(fetchResourceUri());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mGetResourceDetailsByTypeCase.unsubscribe();
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_fv_s);
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

    private Bundle getExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            throw new RuntimeException("Intent extras should be provided");
        }
        return extras;
    }

    private String fetchResourceUri() {
        Bundle extras = getExtras();
        String resourceUri = extras.getString(RESOURCE_URI_ARG);
        if (resourceUri == null) {
            throw new RuntimeException("Resource uri should be provided");
        }
        return resourceUri;
    }

    private void getFileInfo(String uri) {
        ResourceDetailsRequest request = new ResourceDetailsRequest(uri, "file");
        mGetResourceDetailsByTypeCase.execute(request, new FileInfoListener());
    }

    private class FileInfoListener extends SimpleSubscriber<ResourceLookup> {
        @Override
        public void onStart() {
            showProgressDialog();
        }

        @Override
        public void onCompleted() {
            hideProgressDialog();
        }

        @Override
        public void onError(Throwable e) {
            RequestExceptionHandler.showCommonErrorMessage(FileViewerActivity.this, e);
            hideProgressDialog();
        }

        @Override
        public void onNext(ResourceLookup lookup) {
            FileLookup fileLookup = (FileLookup) lookup;
            Fragment fileFragment;
            switch (fileLookup.getFileType()) {
                case html:
                    fileFragment = HtmlFileViewFragment_.builder()
                            .fileType(fileLookup.getFileType())
                            .fileUri(fileLookup.getUri())
                            .build();
                    break;
                case img:
                    fileFragment = ImageFileViewFragment_.builder()
                            .fileType(fileLookup.getFileType())
                            .fileUri(fileLookup.getUri())
                            .build();
                    break;
                case pdf:
                case xls:
                case xlsx:
                case docx:
                case ods:
                case odt:
                case pptx:
                case rtf:
                case csv:
                    fileFragment = ExternalOpenFragment_.builder()
                            .fileType(fileLookup.getFileType())
                            .fileUri(fileLookup.getUri())
                            .build();
                    break;
                default:
                    fileFragment = UnsupportedFilesFragment_.builder()
                            .fileType(fileLookup.getFileType())
                            .fileUri(fileLookup.getUri())
                            .build();
                    break;
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_frame, fileFragment)
                    .commit();

            analytics.sendEvent(
                    Analytics.EventCategory.RESOURCE.getValue(),
                    Analytics.EventAction.VIEWED.getValue(),
                    fileLookup.getFileType().name()
            );

            showFileTitle(lookup.getLabel());
        }
    }
}
