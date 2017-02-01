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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jaspersoft.android.jaspermobile.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public class BreadcrumbsView extends RecyclerView {
    private BreadcrumbClickListener breadcrumbClickListener;
    private BreadcrumbsAdapter breadcrumbsAdapter;

    public BreadcrumbsView(Context context) {
        super(context);
        init();
    }

    public BreadcrumbsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BreadcrumbsView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutManager(new LinearLayoutManager(getContext(), HORIZONTAL, false));
        breadcrumbsAdapter = new BreadcrumbsAdapter(LayoutInflater.from(getContext()));
        super.setAdapter(breadcrumbsAdapter);
    }

    public void setBreadcrumbClickListener(BreadcrumbClickListener breadcrumbClickListener) {
        this.breadcrumbClickListener = breadcrumbClickListener;
    }

    @Override
    public void setAdapter(Adapter adapter) {

    }

    @Override
    public BreadcrumbsAdapter getAdapter() {
        return breadcrumbsAdapter;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.breadcrumbs = breadcrumbsAdapter.getCrumbList();

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        breadcrumbsAdapter.setCrumbList(ss.breadcrumbs);
        updateVisibility();
    }

    private void updateVisibility() {
        setVisibility(breadcrumbsAdapter.getItemCount() > 1 ? VISIBLE : GONE);
    }

    public interface BreadcrumbClickListener {
        void onBreadcrumbClick(int level);
    }

    public class BreadcrumbsAdapter extends RecyclerView.Adapter<BaseViewHolder> {
        private final static int CRUMB = 0;
        private final static int ARROW_DIVIDER = 1;

        private final LayoutInflater layoutInflater;

        private List<String> crumbList;

        private BreadcrumbsAdapter(LayoutInflater layoutInflater) {
            this.layoutInflater = layoutInflater;
            crumbList = new ArrayList<>();
        }

        void setCrumbList(List<String> crumbList) {
            this.crumbList = crumbList;
        }

        List<String> getCrumbList() {
            return crumbList;
        }

        public void addBreadCrumb(String breadCrumb) {
            crumbList.add(breadCrumb);

            int insertedItemIndex = getItemCount() - 1;
            notifyItemInserted(insertedItemIndex);
            scrollToPosition(insertedItemIndex);
            updateVisibility();
        }

        public void removeBreadCrumb() {
            int removedItemIndex = getItemCount() - 1;
            crumbList.remove(crumbList.size() - 1);

            notifyItemRangeRemoved(removedItemIndex - 1, 2);
            scrollToPosition(getItemCount() - 1);
            updateVisibility();
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2 == 0 ? CRUMB : ARROW_DIVIDER;
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == CRUMB) {
                View crumbView = layoutInflater.inflate(R.layout.item_breadcrumb, parent, false);
                return new CrumbsViewHolder(crumbView);
            }
            View arrowView = layoutInflater.inflate(R.layout.item_breadcrumb_arrow, parent, false);
            return new ArrowViewHolder(arrowView);
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            if (position % 2 == 1) return;

            String crumb = crumbList.get(position / 2);
            holder.setCrumbName(crumb);
        }

        @Override
        public int getItemCount() {
            return crumbList.size() * 2 - 1;
        }
    }

    private class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        public void setCrumbName(String crumb) {

        }
    }

    private class CrumbsViewHolder extends BaseViewHolder implements OnClickListener {
        private Button crumbName;

        public CrumbsViewHolder(View itemView) {
            super(itemView);
            crumbName = (Button) itemView.findViewById(R.id.bookmarkCrumb);

            crumbName.setOnClickListener(this);
        }

        @Override
        public void setCrumbName(String crumb) {
            crumbName.setText(crumb);
        }

        @Override
        public void onClick(View v) {
            if (breadcrumbClickListener != null) {
                int level = getAdapterPosition() / 2;
                breadcrumbClickListener.onBreadcrumbClick(level);
            }
        }
    }

    private class ArrowViewHolder extends BaseViewHolder {
        public ArrowViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class SavedState extends BaseSavedState {
        List<String> breadcrumbs;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            in.readStringList(breadcrumbs);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeStringList(breadcrumbs);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
