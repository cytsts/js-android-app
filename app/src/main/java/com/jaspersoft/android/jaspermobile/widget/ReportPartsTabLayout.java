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

package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.sdk.widget.report.renderer.ReportPart;
import com.jaspersoft.android.sdk.widget.report.view.ReportPartsListener;
import com.jaspersoft.android.sdk.widget.report.view.ReportProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public class ReportPartsTabLayout extends TabLayout implements ReportPartsListener, TabLayout.OnTabSelectedListener {
    private ReportPartSelectListener reportPartSelectListener;
    private ReportProperties reportProperties;

    public ReportPartsTabLayout(Context context) {
        super(context);
    }

    public ReportPartsTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReportPartsTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        ViewGroup tabsParent = (ViewGroup) getChildAt(0);
        for (int i = 0; i < tabsParent.getChildCount(); i++) {
            View tab = tabsParent.getChildAt(i);
            tab.setEnabled(enabled);
        }
        setAlpha(enabled ? 1 : 0.5f);
    }

    public void setReportPartSelectListener(ReportPartSelectListener reportPartSelectListener) {
        this.reportPartSelectListener = reportPartSelectListener;
    }

    public void setReportProperties(ReportProperties reportProperties) {
        this.reportProperties = reportProperties;

        onReportPartsChanged(reportProperties.getReportPartList());
        onCurrentReportPartChanged(reportProperties.getCurrentReportPart());
    }

    @Override
    public void onReportPartsChanged(List<ReportPart> reportPartList) {
        setVisibility(!reportPartList.isEmpty() ? View.VISIBLE : View.GONE);

        List<String> reportPartsName = getReportPartNames(reportPartList);
        updateTabsTitles(reportPartsName);
    }

    @Override
    public void onCurrentReportPartChanged(ReportPart reportPart) {
        if (reportPart == null) return;

        String selectedReportPart = reportPart.getName();
        List<String> reportPartsName = getReportPartNames(reportProperties.getReportPartList());
        for (int i = 0; i < reportPartsName.size(); i++) {
            String currentReportPart = reportPartsName.get(i);
            if (selectedReportPart.equals(currentReportPart)) {
                selectTab(i);
            }
        }
    }

    private void updateTabsTitles(List<String> tabs) {
        removeAllTabs();
        setOnTabSelectedListener(null);
        for (String tab : tabs) {
            Tab reportPartTab = newTab().setText(tab);
            addTab(reportPartTab);
        }
        setOnTabSelectedListener(this);
    }

    private void selectTab(int index) {
        Tab tabToSelect = getTabAt(index);
        if (tabToSelect == null) return;

        setOnTabSelectedListener(null);
        tabToSelect.select();
        setOnTabSelectedListener(this);
    }

    private List<String> getReportPartNames(List<ReportPart> reportPartList) {
        List<String> reportPartsName = new ArrayList<>();
        for (ReportPart reportPart : reportPartList) {
            reportPartsName.add(reportPart.getName());
        }
        return reportPartsName;
    }

    public interface ReportPartSelectListener {
        void onReportPartSelected(int index);
    }

    @Override
    public void onTabSelected(Tab tab) {
        if (reportPartSelectListener != null) {
            reportPartSelectListener.onReportPartSelected(tab.getPosition());
        }
    }

    @Override
    public void onTabUnselected(Tab tab) {

    }

    @Override
    public void onTabReselected(Tab tab) {
        if (reportPartSelectListener != null) {
            reportPartSelectListener.onReportPartSelected(tab.getPosition());
        }
    }
}
