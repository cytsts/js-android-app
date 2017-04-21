package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.AdhocDataViewFragment;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;

import dagger.Subcomponent;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

@PerActivity
@Subcomponent(
        modules = {
                ActivityModule.class
        }
)

public interface AdhocDataViewFragmentComponent {
    void inject(AdhocDataViewFragment fragment);
}
