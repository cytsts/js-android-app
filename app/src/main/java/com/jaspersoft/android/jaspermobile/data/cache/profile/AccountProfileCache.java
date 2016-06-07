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

package com.jaspersoft.android.jaspermobile.data.cache.profile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of profile cache around {@link AccountManager}. This cache used in order to persist
 * new profiles in system. Also used for validation purposes of weather profile in cache or not.
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class AccountProfileCache implements ProfileCache {
    private static final String ALIAS_KEY = "ALIAS_KEY";

    private final AccountManager mAccountManager;
    private final AccountDataMapper mAccountDataMapper;

    @Inject
    public AccountProfileCache(AccountManager accountManager, AccountDataMapper accountDataMapper) {
        mAccountManager = accountManager;
        mAccountDataMapper = accountDataMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile put(@NonNull final Profile profile) {
        Account accountProfile = mAccountDataMapper.transform(profile);
        Bundle userData = new Bundle();
        userData.putString(ALIAS_KEY, profile.getKey());
        boolean saved = mAccountManager.addAccountExplicitly(accountProfile, null, userData);
        if (!saved) {
            throw new FailedToSaveProfile(profile);
        }
        return profile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProfile(@NonNull Profile profile) {
        String accountType = mAccountDataMapper.getAccountType();
        Account accountProfile = mAccountDataMapper.transform(profile);
        Account[] accounts = mAccountManager.getAccountsByType(accountType);
        Set<Account> accountsSet = new HashSet<>(Arrays.asList(accounts));
        return accountsSet.contains(accountProfile);
    }

    @Override
    public List<Profile> getAll() {
        String accountType = mAccountDataMapper.getAccountType();
        Account[] accounts = mAccountManager.getAccountsByType(accountType);
        return mAccountDataMapper.transform(accounts);
    }
}
