package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.filters.FiltersFragment;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;

import dagger.Subcomponent;

/**
 * Created by aleksandrdakhno on 7/7/17.
 */

@PerActivity
@Subcomponent(
        modules = {
                ActivityModule.class
        }
)

public interface FiltersFragmentComponent {
    void inject(FiltersFragment fragment);
}
