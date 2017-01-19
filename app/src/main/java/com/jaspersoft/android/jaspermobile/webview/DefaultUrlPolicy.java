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

package com.jaspersoft.android.jaspermobile.webview;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.webresource.WebResourceActivity_;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class DefaultUrlPolicy implements UrlPolicy {

    private final String serverUrl;
    private SessionListener sessionListener;

    public DefaultUrlPolicy(String serverUrl) {
        this.sessionListener = SessionListener.NULL;
        this.serverUrl = serverUrl;
    }

    public DefaultUrlPolicy withSessionListener(SessionListener sessionListener) {
        this.sessionListener = sessionListener;
        return this;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (isLoginRequestUrl(url)) {
            if (sessionListener != null) {
                sessionListener.onSessionExpired();
            }
            return true;
        }

        Context context = view.getContext();
        if (isInternalRequestUrl(url)) {
            showInternalLink(context, url);
        } else {
            showExternalLink(context, url);
        }
        return true;
    }

    private boolean isLoginRequestUrl(String url) {
        // This is my Jasper site, let WebView check page for 401 page
        if (isValidUrl(url) && isInternalRequestUrl(url)) {
            return url.contains("login.html");
        }
        return false;
    }

    private boolean isInternalRequestUrl(String url) {
        String jasperHost = Uri.parse(serverUrl).getHost();
        String linkHost = Uri.parse(url).getHost();
        return linkHost.equals(jasperHost);
    }

    private boolean isValidUrl(String url) {
        String linkHost = Uri.parse(url).getHost();
        return linkHost != null;
    }

    private void showExternalLink(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // show notification if no app available to open selected format
            Toast.makeText(context,
                    context.getString(R.string.sdr_t_no_app_available, "view"),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showInternalLink(Context context, String url) {
        WebResourceActivity_.intent(context)
                .resourceUrl(url).start();
    }

    public interface SessionListener {
        SessionListener NULL = new SessionListener() {
            @Override
            public void onSessionExpired() {
            }
        };
        void onSessionExpired();
    }
}
