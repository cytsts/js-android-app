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

package com.jaspersoft.android.jaspermobile.activities.report.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.ui.view.activity.ToolbarActivity;
import com.jaspersoft.android.jaspermobile.widget.BreadcrumbsView;
import com.jaspersoft.android.sdk.widget.report.renderer.Bookmark;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public class BookmarksActivity extends ToolbarActivity implements BookmarksAdapter.BookmarkSelectListener, FragmentManager.OnBackStackChangedListener, BreadcrumbsView.BreadcrumbClickListener {
    public static final String BOOKMARK_LIST_ARG = "bookmarkList";
    public static final String SELECTED_BOOKMARK_ARG = "selectedBookmark";

    private int breadcrumbsSize;

    @BindView(R.id.bookmarksToolbar)
    Toolbar bookmarksToolbar;
    @BindView(R.id.breadcrumbs)
    BreadcrumbsView breadcrumbs;
    @BindView(R.id.bookmarksListContainer)
    FrameLayout bookmarksListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        ButterKnife.bind(this);
        initToolbar(getString(R.string.rv_ab_bookmarks));

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        breadcrumbsSize = getSupportFragmentManager().getBackStackEntryCount();
        breadcrumbs.setBreadcrumbClickListener(this);

        if (savedInstanceState == null) {
            List<Bookmark> bookmarkList = fetchBookmarks();
            showBookmarks(getString(R.string.s_fd_option_all), bookmarkList, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }

    @Override
    public void onBookmarkSelected(Bookmark bookmark) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SELECTED_BOOKMARK_ARG, bookmark);

        Intent returnIntent = new Intent();
        returnIntent.putExtras(bundle);

        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBookmarkIntoSelected(Bookmark bookmark) {
        showBookmarks(bookmark.getAnchor(), bookmark.getBookmarks(), false);
    }

    @Override
    public void onBackStackChanged() {
        int newBreadcrumbsSize = getSupportFragmentManager().getBackStackEntryCount();
        if (newBreadcrumbsSize < breadcrumbsSize) {
            breadcrumbs.getAdapter().removeBreadCrumb();
        }
        breadcrumbsSize = newBreadcrumbsSize;
    }

    @Override
    public void onBreadcrumbClick(int level) {
        int backStepsCount = breadcrumbsSize - level;
        for (int i = 0; i < backStepsCount; i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @NotNull
    private List<Bookmark> fetchBookmarks() {
        Bundle extras = getIntent().getExtras();
        List<Bookmark> bookmarks = null;
        if (extras != null) {
            bookmarks = extras.getParcelableArrayList(BOOKMARK_LIST_ARG);
        }
        if (bookmarks == null) {
            throw new RuntimeException("Bookmarks should be provided");
        }
        return bookmarks;
    }

    private void initToolbar(String title) {
        bookmarksToolbar.setTitle(title);
        bookmarksToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showBookmarks(String name, List<Bookmark> bookmarksList, boolean isRoot) {
        Fragment bookmarkFragment = BookmarkFragment.create(bookmarksList);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (!isRoot) {
            fragmentTransaction.addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
        fragmentTransaction.replace(R.id.bookmarksListContainer, bookmarkFragment)
                .commit();
        breadcrumbs.getAdapter().addBreadCrumb(name);
    }
}
