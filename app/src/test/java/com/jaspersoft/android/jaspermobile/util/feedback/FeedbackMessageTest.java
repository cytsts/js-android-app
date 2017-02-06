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

package com.jaspersoft.android.jaspermobile.util.feedback;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.db.migrate.ShadowApplicationImpl;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowApplication;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21, constants = BuildConfig.class)
public class FeedbackMessageTest {
    @Mock
    JasperServer mServer;
    Message feedbackMessage;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        feedbackMessage = new Message(RuntimeEnvironment.application, mServer);
    }

    @Test
    public void shouldCreateValidFeedbackMessage() {
        mockPackageManager();

        givenServerVersion("6.1");
        givenServerEditionCE();

        String message = feedbackMessage.create();

        assertThat(message, is(notNullValue()));
    }

    private void givenServerEditionCE() {
        when(mServer.isProEdition()).thenReturn(false);
    }

    private void givenServerVersion(String version) {
        when(mServer.getVersion()).thenReturn(version);
    }

    @Test
    public void shouldGenerateAppVersionInfo() {
        mockPackageManager();
        String message = feedbackMessage.generateAppVersionInfo();
        assertThat(message, containsString("App version"));
    }

    @Test
    @Config(constants = BuildConfig.class, sdk = 21, shadows = {ShadowApplicationImpl.class})
    public void shouldGenerateNullForAppVersionInfo() {
        Application context = RuntimeEnvironment.application;
        RobolectricPackageManager packageManager = (RobolectricPackageManager) ((ShadowApplication)ShadowExtractor.extract(context)).getPackageManager();
        packageManager.removePackage(context.getPackageName());

        String message = feedbackMessage.generateAppVersionInfo();
        assertThat(message, is(nullValue()));
    }

    @Test
    public void shouldGenerateJrsVersionInfo() {
        givenServerVersion("6.1");
        String message = feedbackMessage.generateServerVersion();
        assertThat(message, containsString("JRS version: 6.1"));
    }

    @Test
    public void shouldGenerateJrsEditionInfo() {
        givenServerEditionCE();
        String message = feedbackMessage.generateServerEdition();
        assertThat(message, containsString("JRS edition: CE"));
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void mockPackageManager() {
        Application context = RuntimeEnvironment.application;
        RobolectricPackageManager packageManager = (RobolectricPackageManager) ((ShadowApplication)ShadowExtractor.extract(context)).getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
            info.versionCode = 20100000;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
