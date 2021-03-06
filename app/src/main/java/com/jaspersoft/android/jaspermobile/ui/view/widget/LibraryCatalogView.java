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

package com.jaspersoft.android.jaspermobile.ui.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EViewGroup;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EViewGroup(R.layout.fragment_refreshable_resource)
public class LibraryCatalogView extends CatalogView {
    public LibraryCatalogView(Context context) {
        super(context);
    }

    public LibraryCatalogView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LibraryCatalogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void showError() {
        super.showError();
        message.setVisibility(View.VISIBLE);
        message.setText(getContext().getString(R.string.failed_load_data));
    }

    @Override
    public void showEmpty() {
        message.setVisibility(View.VISIBLE);
        message.setText(getContext().getString(R.string.resources_not_found));
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mAnalytics.sendEvent(Analytics.EventCategory.CATALOG.getValue(), Analytics.EventAction.REFRESHED.getValue(), Analytics.EventLabel.CHOOSE_REPORT.getValue());
    }
}
