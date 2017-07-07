package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.filters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.webresource.SingleFragmentActivity;

/**
 * Created by aleksandrdakhno on 7/7/17.
 */

public class FiltersActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context, String filtersUri) {
        Intent i = new Intent(context, FiltersActivity.class);
        i.putExtra(FiltersFragment.ARG_FILTERS_URI, filtersUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return FiltersFragment.newInstance(getIntent().getExtras().getString(FiltersFragment.ARG_FILTERS_URI));
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_f_s);
    }
}
