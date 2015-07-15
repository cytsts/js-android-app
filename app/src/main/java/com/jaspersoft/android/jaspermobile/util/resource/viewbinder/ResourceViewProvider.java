package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.view.ViewGroup;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public interface ResourceViewProvider {

    BaseViewHolder provideListViewHolder(ViewGroup parent);
    BaseViewHolder provideGridViewHolder(ViewGroup parent);
}
