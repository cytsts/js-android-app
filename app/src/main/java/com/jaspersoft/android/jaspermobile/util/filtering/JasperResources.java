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

package com.jaspersoft.android.jaspermobile.util.filtering;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import java.util.ArrayList;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.*;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperResources {

    public static ArrayList<String> folder() {
        return JasperFilter.FOLDER.getAsList();
    }

    public static ArrayList<String> report() {
        return JasperFilter.REPORT.getAsList();
    }

    public static ArrayList<String> files() {
        return JasperFilter.FILES.getAsList();
    }

    public static ArrayList<String> dashboard(ServerVersion version) {
        boolean isPreAmber = version.lessThan(ServerVersion.v6);
        if (isPreAmber) {
            return JasperFilter.DASHBOARD_PRE_AMBER.getAsList();
        } else {
            return JasperFilter.DASHBOARD_AMBER.getAsList();
        }
    }

    public static ArrayList<String> adhocDataView() {
        return JasperFilter.ADHOC_DATA_VIEW.getAsList();
    }

    private enum JasperFilter {
        FOLDER(folder),
        REPORT(reportUnit),
        DASHBOARD_PRE_AMBER(dashboard),
        DASHBOARD_AMBER(legacyDashboard, dashboard),
        FILES(file),
        ADHOC_DATA_VIEW(adhocDataView);

        private final ArrayList<String> mTypes = new ArrayList<String>();

        JasperFilter(ResourceLookup.ResourceType... types) {
            for (ResourceLookup.ResourceType type : types) {
                mTypes.add(type.toString());
            }
        }

        ArrayList<String> getAsList() {
            return mTypes;
        }
    }
}
