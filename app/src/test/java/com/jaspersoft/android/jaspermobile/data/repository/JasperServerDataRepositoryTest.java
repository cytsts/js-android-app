/*
 * Copyright � 2016 TIBCO Software,Inc.All rights reserved.
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

package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.data.cache.profile.JasperServerCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.JasperServerMapper;
import com.jaspersoft.android.jaspermobile.data.repository.profile.JasperServerDataRepository;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class JasperServerDataRepositoryTest {

    @Mock
    JasperServerCache mJasperServerCache;
    @Mock
    JasperServerMapper mJasperServerMapper;

    JasperServerDataRepository repoUnderTest;
    Profile fakeProfile;
    JasperServer fakeServer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Server.Builder builder = Server.builder();
        repoUnderTest = new JasperServerDataRepository(mJasperServerCache, mJasperServerMapper, builder);
        fakeProfile = Profile.create("name");
        fakeServer = new JasperServer.Builder()
                .setBaseUrl("http://localhost")
                .setVersion(ServerVersion.v6.toString())
                .setEdition("PRO")
                .create();
    }

    @Test
    public void testSaveServer() throws Exception {
        repoUnderTest.saveServer(fakeProfile, fakeServer);
        verify(mJasperServerCache).put(fakeProfile, fakeServer);
    }

    @Test
    public void testGetServer() throws Exception {
        repoUnderTest.getServer(fakeProfile);
        verify(mJasperServerCache).get(fakeProfile);
    }
}