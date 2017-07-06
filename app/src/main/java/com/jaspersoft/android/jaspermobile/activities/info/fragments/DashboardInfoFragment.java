/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.info.fragments;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.save.SaveDashboardActivity_;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
@OptionsMenu({R.menu.save_item_menu})
@EFragment(R.layout.fragment_resource_info)
public class DashboardInfoFragment extends ResourceInfoFragment{

    @OptionsMenuItem(R.id.saveAction)
    protected MenuItem saveOptions;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        ServerVersion serverVersion = ServerVersion.valueOf(mJasperServer.getVersion());
        saveOptions.setVisible(serverVersion.greaterThan(ServerVersion.v6_2_1) && mResourceLookup != null);
    }

    @OptionsItem(R.id.saveAction)
    protected void saveReport() {
        if (FileUtils.isExternalStorageWritable()) {
            SaveDashboardActivity_.intent(this)
                    .resource(mResourceLookup)
                    .start();
        } else {
            Toast.makeText(getActivity(),
                    R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
    }
}
