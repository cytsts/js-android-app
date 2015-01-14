/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.network.endpoint;

import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import retrofit.Endpoint;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class DemoEndpoint implements Endpoint {
    public static final String DEFAULT_ENDPOINT = "http://mobiledemo.jaspersoft.com/jasperserver-pro";
    public static final String DEFAULT_USERNAME = "phoneuser";
    public static final String DEFAULT_PASSWORD = "phoneuser";

    @Override
    public String getUrl() {
        return DEFAULT_ENDPOINT + JasperSettings.DEFAULT_REST_VERSION;
    }

    @Override
    public String getName() {
        return "Mobile demo endpoint";
    }
}
