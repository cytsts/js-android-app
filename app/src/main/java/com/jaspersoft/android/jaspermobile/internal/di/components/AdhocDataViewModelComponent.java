package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.AdhocDataViewModel;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;

import dagger.Subcomponent;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

@PerActivity
@Subcomponent(
        modules = {
                ActivityModule.class
        }
)

public interface AdhocDataViewModelComponent {
    void inject(AdhocDataViewModel model);
}
