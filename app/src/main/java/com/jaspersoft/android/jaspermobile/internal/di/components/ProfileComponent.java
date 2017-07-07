/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.activities.save.ReportDownloadManager;
import com.jaspersoft.android.jaspermobile.activities.save.SaveReportService;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.internal.di.components.screen.ChooseReportScreenComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.screen.JobFormScreenComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.screen.JobInfoScreenComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.screen.JobsScreenComponent;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.DashboardModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.NavigationActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ReportModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ReportViewModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.screen.job.JobFormScreenModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.screen.job.JobInfoScreenModule;
import com.jaspersoft.android.jaspermobile.util.cast.ResourcePresentationService;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.FileResourceBinder;

import dagger.Subcomponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
@Subcomponent(
        modules = ProfileModule.class
)
public interface ProfileComponent {

    ReportComponent plusReportViewer(ActivityModule activityModule,
                                     ReportViewModule reportModule);

    ControlsActivityComponent plusControlsPage(ActivityModule activityModule,
                                               ReportModule reportModule);

    DashboardActivityComponent plusDashboardPage(ActivityModule activityModule,
                                                DashboardModule dashboardModule);

    NavigationActivityComponent plusNavigationPage(NavigationActivityModule module);

    BaseActivityComponent plusBase(ActivityModule activityModule);

    ChooseReportScreenComponent newChooseReportScreen();

    JobInfoScreenComponent plus(JobInfoScreenModule module);

    JobsScreenComponent newJobsScreen();

    AdhocDataViewFragmentComponent plusAdhocDataViewPage();
    AdhocDataViewWebEnvironmentComponent plusAdhocDataViewWebEnvironment();
    FiltersFragmentComponent plusFiltersPage();

    JobFormScreenComponent plus(JobFormScreenModule module);

    Profile getProfile();
    /**
     * TODO remove one after architecture will be revised
     * Hardcoded injections.
     */
    void inject(FileResourceBinder fileResourceBinder);
    void inject(SaveReportService saveReportService);
    void inject(ResourcePresentationService resourcePresentationService);
    void inject(ReportDownloadManager reportDownloadManager);
}
