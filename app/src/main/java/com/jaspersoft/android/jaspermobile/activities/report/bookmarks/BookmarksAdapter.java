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
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.widget.report.renderer.Bookmark;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public class BookmarksAdapter extends BaseAdapter implements View.OnClickListener {
    private final BookmarkSelectListener bookmarkSelectListener;
    private final LayoutInflater layoutInflater;
    private final List<Bookmark> bookmarkList;

    public BookmarksAdapter(Context context, BookmarkSelectListener bookmarkSelectListener, List<Bookmark> bookmarkList) {
        if (context == null) {
            throw new IllegalArgumentException("Context should be provided");
        }
        if (bookmarkSelectListener == null) {
            throw new IllegalArgumentException("BookmarkSelectListener should be provided");
        }
        if (bookmarkList == null) {
            throw new IllegalArgumentException("Bookmark list should be provided");
        }

        this.bookmarkSelectListener = bookmarkSelectListener;
        this.bookmarkList = bookmarkList;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return bookmarkList.size();
    }

    @Override
    public Bookmark getItem(int position) {
        return bookmarkList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = layoutInflater.inflate(R.layout.item_bookmark, parent, false);
            holder = new ViewHolder(convertView);
            holder.intoBookmarks.getDrawable().mutate().setColorFilter(convertView.getContext().getResources().getColor(R.color.js_light_gray), PorterDuff.Mode.MULTIPLY);

            convertView.setTag(holder);
            convertView.setOnClickListener(this);
            holder.intoBookmarks.setOnClickListener(new BookmarkIntoListener());
        }

        Bookmark bookmark = getItem(position);

        holder.bookmarkName.setText(bookmark.getAnchor());
        holder.bookmarkPage.setText(parent.getContext().getString(R.string.rv_bookmark_page, bookmark.getPage()));
        holder.intoBookmarks.setVisibility(bookmark.getBookmarks().isEmpty()  ? View.GONE : View.VISIBLE);
        holder.position = position;

        return convertView;
    }

    @Override
    public void onClick(View v) {
        int position = ((ViewHolder) v.getTag()).position;
        Bookmark bookmark = getItem(position);
        bookmarkSelectListener.onBookmarkSelected(bookmark);
    }

    public interface BookmarkSelectListener {
        void onBookmarkSelected(com.jaspersoft.android.sdk.widget.report.renderer.Bookmark bookmark);

        void onBookmarkIntoSelected(com.jaspersoft.android.sdk.widget.report.renderer.Bookmark bookmark);
    }

    private class BookmarkIntoListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            View parent = (View) v.getParent();
            ViewHolder viewHolder = (ViewHolder) parent.getTag();
            int position = viewHolder.position;
            Bookmark bookmark = getItem(position);
            bookmarkSelectListener.onBookmarkIntoSelected(bookmark);
        }
    }

    static class ViewHolder {
        int position;

        @BindView(R.id.bookmarkName)
        TextView bookmarkName;
        @BindView(R.id.bookmarkPage)
        TextView bookmarkPage;
        @BindView(R.id.intoBookmarks)
        ImageButton intoBookmarks;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
