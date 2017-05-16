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

package com.jaspersoft.android.jaspermobile.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.widget.report.view.ReportPaginationListener;
import com.jaspersoft.android.sdk.widget.report.view.ReportProperties;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public class SimplePaginationView extends RelativeLayout implements ReportPaginationListener {
    private ReportProperties reportProperties;
    private PageSelectListener pageSelectListener;

    @BindView(R.id.pageValues)
    View pageValues;
    @BindView(R.id.firstPage)
    ImageButton firstPage;
    @BindView(R.id.previousPage)
    ImageButton previousPage;
    @BindView(R.id.nextPage)
    ImageButton nextPage;
    @BindView(R.id.lastPage)
    ImageButton lastPage;
    @BindView(R.id.currentPage)
    TextView currentPageLabel;
    @BindView(R.id.totalPageLabel)
    TextView totalPagesLabel;
    @BindView(R.id.paginationLoading)
    ProgressBar loading;

    public SimplePaginationView(Context context) {
        super(context);
        init();
    }

    public SimplePaginationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimplePaginationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SimplePaginationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setPageSelectListener(PageSelectListener pageSelectListener) {
        this.pageSelectListener = pageSelectListener;
    }

    public void setReportProperties(ReportProperties reportProperties) {
        this.reportProperties = reportProperties;

        onPagesCountChanged(reportProperties.getPagesCount());
        onCurrentPageChanged(reportProperties.getCurrentPage());
        onMultiPageStateChange(reportProperties.isMultiPage());
        setEnabled(isEnabled());
    }

    @Override
    public void onPagesCountChanged(Integer totalPages) {
        setEnabled(isEnabled());

        totalPagesLabel.setText(totalPages == null ? "" : getContext().getString(R.string.of, reportProperties.getPagesCount()));
        loading.setVisibility(totalPages == null ? VISIBLE : GONE);
    }

    @Override
    public void onCurrentPageChanged(int currentPage) {
        setEnabled(isEnabled());

        this.currentPageLabel.setText(String.valueOf(currentPage));
    }

    @Override
    public void onMultiPageStateChange(boolean isMultiPage) {
        setVisibility(isMultiPage ? VISIBLE : GONE);
    }

    @OnClick(R.id.firstPage)
    void firstPageClick() {
        notifyPageSelected(1);
    }

    @OnClick(R.id.previousPage)
    void previousPageClick() {
        notifyPageSelected(reportProperties.getCurrentPage() - 1);
    }

    @OnClick(R.id.nextPage)
    void nextPageClick() {
        notifyPageSelected(reportProperties.getCurrentPage() + 1);
    }

    @OnClick(R.id.lastPage)
    void lastPageClick() {
        notifyPageSelected(reportProperties.getPagesCount());
    }

    @OnClick(R.id.pagesNumberContainer)
    void pagesNumberClick() {
        if (pageSelectListener != null) {
            pageSelectListener.onRemotePageSelected(reportProperties.getPagesCount());
        }
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_simple_pagination, this);
        ButterKnife.bind(this);
        setVisibility(GONE);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        Integer currentPage = reportProperties == null ? 1 : reportProperties.getCurrentPage();
        Integer pagesCount = reportProperties == null ? null : reportProperties.getPagesCount();

        firstPage.setEnabled(currentPage != 1 && enabled);
        previousPage.setEnabled(currentPage != 1 && enabled);
        nextPage.setEnabled(!currentPage.equals(pagesCount) && enabled);
        lastPage.setEnabled(pagesCount != null && !currentPage.equals(pagesCount) && enabled);
        pageValues.setEnabled(enabled);

        firstPage.setAlpha(firstPage.isEnabled() ? 1f : 0.5f);
        previousPage.setAlpha(previousPage.isEnabled() ? 1f : 0.5f);
        nextPage.setAlpha(nextPage.isEnabled() ? 1f : 0.5f);
        lastPage.setAlpha(lastPage.isEnabled() ? 1f : 0.5f);
        pageValues.setAlpha(pageValues.isEnabled() ? 1f : 0.5f);
    }

    private void notifyPageSelected(int page) {
        if (pageSelectListener != null) {
            pageSelectListener.onPageSelected(page);
        }
    }

    public interface PageSelectListener {
        void onPageSelected(int page);
        void onRemotePageSelected(Integer pagesCount);
    }
}
