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

package com.jaspersoft.android.jaspermobile.activities.info;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.DashboardInfoFragment_;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.FileInfoFragment_;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.ReportInfoFragment_;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.ResourceInfoFragment;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.ResourceInfoFragment_;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.SavedItemInfoFragment_;
import com.jaspersoft.android.jaspermobile.ui.view.activity.ToolbarActivity;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EActivity
public class ResourceInfoActivity extends ToolbarActivity {

    @Extra
    protected JasperResource jasperResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment resourceInfoFragment = getSupportFragmentManager().findFragmentByTag(ResourceInfoFragment.TAG);

        if (resourceInfoFragment == null) {
            switch (jasperResource.getResourceType()) {
                case report:
                    resourceInfoFragment = ReportInfoFragment_.builder()
                            .jasperResource(jasperResource)
                            .build();
                    break;
                case dashboard:
                    resourceInfoFragment = DashboardInfoFragment_.builder()
                            .jasperResource(jasperResource)
                            .build();
                    break;
                case file:
                    resourceInfoFragment = FileInfoFragment_.builder()
                            .jasperResource(jasperResource)
                            .build();
                    break;
                case saved_item:
                    resourceInfoFragment = SavedItemInfoFragment_.builder()
                            .jasperResource(jasperResource)
                            .build();
                    break;
                default:
                    resourceInfoFragment = ResourceInfoFragment_.builder()
                            .jasperResource(jasperResource)
                            .build();
            }
        }
        commitContent(resourceInfoFragment, ResourceInfoFragment.TAG);
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_ri_s);
    }

    private void commitContent(@NonNull Fragment directFragment, String fragmentTag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction
                .replace(android.R.id.content, directFragment, fragmentTag)
                .commit();
    }

}
