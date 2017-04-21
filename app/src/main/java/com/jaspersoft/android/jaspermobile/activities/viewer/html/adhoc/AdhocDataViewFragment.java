package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public class AdhocDataViewFragment extends Fragment {

    public static final String ARG_RESOURCE_LOOKUP = "resource_lookup";

    private ResourceLookup resourceLookup;

    public static AdhocDataViewFragment newInstance(ResourceLookup resource) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_RESOURCE_LOOKUP, resource);
        AdhocDataViewFragment fragment = new AdhocDataViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        resourceLookup = getArguments().getParcelable(ARG_RESOURCE_LOOKUP);
        Log.d("AdhocDataViewFragment", "onCreate");
    }
}
