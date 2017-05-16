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

package com.jaspersoft.android.jaspermobile.util.filtering;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
@EBean
public class StorageResourceFilter extends ResourceFilter {

    public enum FileType {
        HTML,
        PNG,
        PDF,
        XLS,
        UNKNOWN
    }

    @RootContext
    protected FragmentActivity activity;

    private enum StorageFilterCategory {
        all(R.string.s_fd_option_all),
        html(R.string.si_fd_option_html),
        png(R.string.si_fd_option_png),
        pdf(R.string.si_fd_option_pdf),
        xls(R.string.si_fd_option_xls);

        private int mTitleId = -1;

        StorageFilterCategory(int titleId) {
            mTitleId = titleId;
        }

        public String getLocalizedTitle(Context context) {
            return context.getString(this.mTitleId);
        }
    }

    @Override
    public String getFilterLocalizedTitle(Filter filter) {
        StorageFilterCategory storageFilterCategory = StorageFilterCategory.valueOf(filter.getName());
        return storageFilterCategory.getLocalizedTitle(activity);
    }

    @Override
    protected List<Filter> generateAvailableFilterList() {
        ArrayList<Filter> availableFilters = new ArrayList<>();

        availableFilters.add(getFilterAll());
        availableFilters.add(getFilterHtml());
        availableFilters.add(getFilterPng());
        availableFilters.add(getFilterPdf());
        availableFilters.add(getFilterXls());

        return availableFilters;
    }

    @Override
    protected FilterStorage initFilterStorage() {
        return LibraryFilterStorage_.getInstance_(activity);
    }

    @Override
    protected Filter getDefaultFilter() {
        return getFilterAll();
    }

    private Filter getFilterAll(){
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.add(FileType.HTML.toString());
        filterValues.add(FileType.PNG.toString());
        filterValues.add(FileType.PDF.toString());
        filterValues.add(FileType.XLS.toString());

        return new Filter(StorageFilterCategory.all.name(), filterValues);
    }

    private Filter getFilterHtml(){
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.add(FileType.HTML.toString());

        return new Filter(StorageFilterCategory.html.name(), filterValues);
    }

    private Filter getFilterPng(){
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.add(FileType.PNG.toString());

        return new Filter(StorageFilterCategory.png.name(), filterValues);
    }

    private Filter getFilterPdf(){
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.add(FileType.PDF.toString());

        return new Filter(StorageFilterCategory.pdf.name(), filterValues);
    }

    private Filter getFilterXls(){
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.add(FileType.XLS.toString());

        return new Filter(StorageFilterCategory.xls.name(), filterValues);
    }
}
