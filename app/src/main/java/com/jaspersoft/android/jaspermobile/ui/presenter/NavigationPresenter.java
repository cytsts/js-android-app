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

package com.jaspersoft.android.jaspermobile.ui.presenter;

import com.jaspersoft.android.jaspermobile.data.ComponentManager;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadataCollection;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetActiveProfileUseCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetProfilesMetadataUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.ui.contract.NavigationContract;
import com.jaspersoft.android.jaspermobile.ui.model.ProfileViewModel;
import com.jaspersoft.android.jaspermobile.ui.model.mapper.ProfileViewModelMapper;
import com.jaspersoft.android.jaspermobile.ui.navigation.Navigator;
import com.jaspersoft.android.jaspermobile.ui.navigation.PageFactory;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class NavigationPresenter extends LegacyPresenter<NavigationContract.View> implements NavigationContract.ActionListener {
    private final CookieHandler mCookieHandler;
    private final Navigator mNavigator;
    private final PageFactory mPageFactory;
    private final ComponentManager mComponentManager;
    private final ProfileViewModelMapper mProfileViewModelMapper;
    private final GetProfilesMetadataUseCase mGetProfilesMetadataUseCase;
    private final GetActiveProfileUseCase mGetActiveProfileUseCase;

    @Inject
    public NavigationPresenter(
            CookieHandler cookieHandler,
            Navigator navigator,
            PageFactory pageFactory,
            ComponentManager componentManager,
            ProfileViewModelMapper profileViewModelMapper,
            GetProfilesMetadataUseCase getProfilesMetadataUseCase,
            GetActiveProfileUseCase getActiveProfileUseCase) {
        mCookieHandler = cookieHandler;
        mNavigator = navigator;
        mPageFactory = pageFactory;
        mComponentManager = componentManager;
        mProfileViewModelMapper = profileViewModelMapper;
        mGetProfilesMetadataUseCase = getProfilesMetadataUseCase;
        mGetActiveProfileUseCase = getActiveProfileUseCase;
    }

    @Override
    public void destroy() {
        mGetProfilesMetadataUseCase.unsubscribe();
        mGetActiveProfileUseCase.unsubscribe();
    }

    @Override
    public void loadProfiles() {
        mGetProfilesMetadataUseCase.execute(new SimpleSubscriber<ProfileMetadataCollection>() {
            @Override
            public void onError(Throwable e) {
                Timber.e(e, "GetProfilesUseCase# failed");
            }

            @Override
            public void onNext(ProfileMetadataCollection collection) {
                showProfiles(collection);
            }
        });
    }

    private void showProfiles(ProfileMetadataCollection collection) {
        List<ProfileViewModel> profiles = mProfileViewModelMapper.transform(collection.get());
        getView().showProfiles(profiles);
    }

    @Override
    public void loadActiveProfile() {
        mGetActiveProfileUseCase.execute(new SimpleSubscriber<ProfileMetadata>() {
            @Override
            public void onError(Throwable e) {
                Timber.e(e, "GetActiveProfileUseCase# failed");
            }

            @Override
            public void onNext(ProfileMetadata profile) {
                JasperServer server = profile.getServer();
                boolean proEdition = server.isProEdition();
            }
        });
    }

    @Override
    public void activateProfile(Profile profile) {
        flushCookies();
        mComponentManager.setupActiveProfile(profile);
        mNavigator.navigate(mPageFactory.createMainPage(), true);
    }

    private void flushCookies() {
        if (mCookieHandler instanceof CookieManager) {
            CookieManager cookieHandler = (CookieManager) mCookieHandler;
            CookieStore cookieStore = cookieHandler.getCookieStore();

            if (cookieStore != null) {
                cookieStore.removeAll();
            }
        }
    }
}
