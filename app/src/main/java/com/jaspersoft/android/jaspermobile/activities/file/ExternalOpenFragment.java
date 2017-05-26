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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EFragment;

import java.io.File;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_file_open)
public class ExternalOpenFragment extends FileLoadFragment {

    protected Button tryToOpen;
    protected TextView messageView;

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tryToOpen = (Button) view.findViewById(R.id.btnTryToOpen);
        messageView = (TextView) view.findViewById(android.R.id.message);

        tryToOpen.setOnClickListener(new TryAgainClickListener());
        tryToShowFile();
    }

    @Override
    protected void onFileReady(File file) {
        openFile(file);
    }

    @Override
    protected void showErrorMessage() {
        messageView.setText(R.string.fv_can_not_show_message);
    }

    private void showFileUnsupportedMessage() {
        messageView.setText(getString(R.string.sdr_t_no_app_available, fileType));
    }

    private void showFileOpeningMessage() {
        messageView.setText(R.string.fv_opening_message);
    }

    private void tryToShowFile() {
        showFileOpeningMessage();
        tryToOpen.setVisibility(View.GONE);

        loadFile();
    }

    private void openFile(File file) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileType.name());


        Uri outputPath = FileProvider.getUriForFile(
                                    getContext(),
                                    getContext().getApplicationContext().getPackageName() + ".fileprovider", file);

        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(outputPath, mimeType);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            getActivity().startActivity(openIntent);
            getActivity().finish();
        } catch (ActivityNotFoundException e) {
            showFileUnsupportedMessage();
            tryToOpen.setVisibility(View.VISIBLE);
        }
    }

    private class TryAgainClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            tryToShowFile();
        }
    }
}
