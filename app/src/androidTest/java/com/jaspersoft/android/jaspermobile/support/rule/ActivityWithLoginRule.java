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

package com.jaspersoft.android.jaspermobile.support.rule;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.widget.ProgressBar;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.BaseReportActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public class ActivityWithLoginRule<A extends Activity> extends ActivityTestRule<A> {
    private final AuthRuleDelegate authRuleDelegate;

    public ActivityWithLoginRule(Class<A> activityClass) {
        super(activityClass);
        authRuleDelegate = new AuthRuleDelegate();
    }

    public ActivityWithLoginRule(Class<A> activityClass, boolean initialTouchMode) {
        super(activityClass, initialTouchMode);
        authRuleDelegate = new AuthRuleDelegate();
    }

    public ActivityWithLoginRule(Class<A> activityClass, boolean initialTouchMode, boolean launchActivity) {
        super(activityClass, initialTouchMode, launchActivity);
        authRuleDelegate = new AuthRuleDelegate();
    }

    @Override
    protected void beforeActivityLaunched() {
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (activity instanceof BaseReportActivity) {
                    ProgressBar progressBar = ((ProgressBar) activity.findViewById(R.id.progressLoading));
                    if (progressBar != null) {
                        progressBar.setIndeterminateDrawable(new ColorDrawable(0xffffffff));
                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    @Override
    protected void afterActivityLaunched() {
        authRuleDelegate.delegateAfterActivityLaunched();
    }
}
