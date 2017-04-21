package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.webresource.SingleFragmentActivity;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public class AdhocDataViewActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context, ResourceLookup resource) {
        Intent i = new Intent(context, AdhocDataViewActivity.class);
        i.putExtra(AdhocDataViewFragment.ARG_RESOURCE_LOOKUP, resource);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return AdhocDataViewFragment.newInstance((ResourceLookup) getIntent().getExtras().getParcelable(AdhocDataViewFragment.ARG_RESOURCE_LOOKUP));
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_adv_s);
    }
}
