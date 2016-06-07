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

package com.jaspersoft.android.jaspermobile.webview.dashboard.bridge;

import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.util.ScreenUtil;
import com.jaspersoft.android.jaspermobile.util.ScreenUtil_;
import com.jaspersoft.android.jaspermobile.webview.dashboard.flow.WebFlowFactory;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class AmberDashboardExecutor extends AbstractDashboardExecutor {
    private final WebView webView;
    private final JasperServer mServer;
    private final ResourceLookup resource;

    private AmberDashboardExecutor(WebView webView, JasperServer server, ResourceLookup resource) {
        this.webView = webView;
        mServer = server;
        this.resource = resource;
    }

    public static DashboardExecutor newInstance(WebView webView, JasperServer server, ResourceLookup resource) {
        if (webView == null) {
            throw new IllegalArgumentException("WebView should not be null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("ResourceLookup should not be null");
        }
        return new AmberDashboardExecutor(webView, server, resource);
    }

    @Override
    void doPreparation() {
        new WebFlowFactory(mServer)
                .createFlow(resource)
                .load(webView);
    }

    @Override
    void doExecution() {
        ScreenUtil screenUtil = ScreenUtil_.getInstance_(webView.getContext());
        String runScript = String.format(
                "javascript:MobileDashboard.configure({\"diagonal\": \"%s\"}).run()",
                screenUtil.getDiagonal());
        webView.loadUrl(runScript);
    }
}
