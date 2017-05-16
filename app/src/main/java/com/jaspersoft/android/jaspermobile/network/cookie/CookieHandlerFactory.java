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

package com.jaspersoft.android.jaspermobile.network.cookie;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.webkit.CookieSyncManager;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class CookieHandlerFactory {
    @Inject
    public CookieHandlerFactory() {
    }

    @NonNull
    public CookieHandler newStore(@NonNull Context context) {
        WebViewCookieStore webViewCookieStore = createWebViewCookieStore(context);
        java.net.CookieStore cookieStore = new PersistentCookieStore(context);

        AppCookieStore appCookieStore = new AppCookieStore(webViewCookieStore, cookieStore);

        CookieHandler defaultCookieManager = new CookieManager(appCookieStore, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(defaultCookieManager);

        return defaultCookieManager;
    }

    private WebViewCookieStore createWebViewCookieStore(@NonNull Context context) {
        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new WebViewCookieStoreLollipop(cookieManager);
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            return new WebViewCookieStorePreLollipop(cookieManager, cookieSyncManager);
        }
    }
}
