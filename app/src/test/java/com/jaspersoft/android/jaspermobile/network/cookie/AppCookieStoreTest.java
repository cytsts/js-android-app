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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.HttpCookie;
import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class AppCookieStoreTest {

    public static final URI DOMAIN = URI.create("http://localhost");
    public static final HttpCookie HTTP_COOKIE = new HttpCookie("key", "name");
    @Mock
    WebViewCookieStore mWebViewCookieStore;
    @Mock
    java.net.CookieStore mStore;

    private AppCookieStore appCookieStore;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        appCookieStore = new AppCookieStore(mWebViewCookieStore, mStore);
    }

    @Test
    public void testAdd() throws Exception {
        appCookieStore.add(DOMAIN, HTTP_COOKIE);
        verify(mStore).add(DOMAIN, HTTP_COOKIE);
        verify(mWebViewCookieStore).add(DOMAIN.toString(), HTTP_COOKIE.toString());
    }

    @Test
    public void testGet() throws Exception {
        appCookieStore.get(DOMAIN);
        verify(mStore).get(DOMAIN);
    }

    @Test
    public void testGetCookies() throws Exception {
        appCookieStore.getCookies();
        verify(mStore).getCookies();
    }

    @Test
    public void testGetURIs() throws Exception {
        appCookieStore.getURIs();
        verify(mStore).getURIs();
    }

    @Test
    public void testRemove() throws Exception {
        appCookieStore.remove(DOMAIN, HTTP_COOKIE);
        verify(mStore).remove(DOMAIN, HTTP_COOKIE);
        verify(mWebViewCookieStore).removeCookie(DOMAIN.toString());
    }

    @Test
    public void testRemoveAll() throws Exception {
        appCookieStore.removeAll();
        verify(mStore).removeAll();
        verify(mWebViewCookieStore).removeAllCookies();
    }
}