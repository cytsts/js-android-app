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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.BaseFragment;
import com.jaspersoft.android.sdk.widget.report.renderer.Bookmark;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public class BookmarkFragment extends BaseFragment {

    private BookmarksAdapter bookmarksAdapter;
    private BookmarksAdapter.BookmarkSelectListener listener;

    public static BookmarkFragment create(List<Bookmark> bookmarkList) {
        BookmarkFragment bookmarkFragment = new BookmarkFragment();

        Bundle args = new Bundle();
        ArrayList<Bookmark> bookmarksArrayList = new ArrayList<>(bookmarkList);
        args.putParcelableArrayList(BookmarksActivity.BOOKMARK_LIST_ARG, bookmarksArrayList);
        bookmarkFragment.setArguments(args);

        return bookmarkFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof BookmarksAdapter.BookmarkSelectListener) {
            listener = (BookmarksAdapter.BookmarkSelectListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement BookmarksAdapter.BookmarkSelectListener.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Bookmark> bookmarksList = fetchBookmarks();
        bookmarksAdapter = new BookmarksAdapter(getContext(), listener, bookmarksList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ListView bookmarks = (ListView) view.findViewById(R.id.bookmarksList);
        bookmarks.setAdapter(bookmarksAdapter);
    }

    @NotNull
    private List<Bookmark> fetchBookmarks() {
        Bundle extras = getArguments();
        List<Bookmark> bookmarks = null;
        if (extras != null) {
            bookmarks = extras.getParcelableArrayList(BookmarksActivity.BOOKMARK_LIST_ARG);
        }
        if (bookmarks == null) {
            throw new RuntimeException("Bookmarks should be provided");
        }
        return bookmarks;
    }
}
