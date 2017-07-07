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

package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.service.data.dashboard.DashboardControlComponent;

import java.util.List;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ControlsRepository {
    @NonNull
    Observable<List<InputControl>> listReportControls(@NonNull String reportUri);

    @NonNull
    Observable<List<InputControl>> listDashboardControls(@NonNull String reportUri);

    @NonNull
    Observable<List<InputControl>> listAdhocDataViewFilters(@NonNull String resourceUri);

    @NonNull
    Observable<List<InputControlState>> validateReportControls(@NonNull String reportUri);

    @NonNull
    Observable<List<InputControlState>> validateDashboardControls(String dashboardUri);

    @NonNull
    Observable<List<InputControlState>> listControlValues(@NonNull String reportUri);

    @NonNull
    Observable<List<DashboardControlComponent>> listDashboardControlComponents(@NonNull String dashboardUri);

    void flushControls(@NonNull String reportUri);

}
