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

package com.jaspersoft.android.jaspermobile.util.account;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Pair;

import com.orhanobut.hawk.Storage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.2.2
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AccountStorageTest {

    private Storage accountStorage;
    private AccountManager accountManager;
    private Account fakeAccount;

    @Before
    public void setUp() throws Exception {
        accountManager = AccountManager.get(RuntimeEnvironment.application);
        fakeAccount = new Account("test", "com.test");
        accountStorage = new AccountStorage(accountManager, fakeAccount);
    }

    @Test
    public void testPut() throws Exception {
        accountStorage.put("bar", "foo");
        String bar = accountManager.getUserData(fakeAccount, "bar");
        assertThat("Failed to put single value in account storage", bar != null);
    }

    @Test
    public void testPutPassword() throws Exception {
        accountStorage.put(AccountStorage.KEY, "foo");
        String bar = accountManager.getPassword(fakeAccount);
        assertThat("Failed to put password account storage", bar != null);
    }

    @Test
    public void testPutPairs() throws Exception {
        Pair<String, ?> pair1 = Pair.create("bar", "foo");
        Pair<String, ?> pair2 = Pair.create("foo", "bar");
        List<Pair<String, ?>> pairs = Arrays.asList(pair1, pair2);
        accountStorage.put(pairs);
        String foo = accountManager.getUserData(fakeAccount, "foo");
        String bar = accountManager.getUserData(fakeAccount, "bar");
        assertThat(
                "Failed to put list of values in account storage",
                foo != null && bar != null
        );
    }

    @Test
    public void testGet() throws Exception {
        accountManager.setUserData(fakeAccount, "foo", "bar");
        String bar = accountStorage.get("foo");
        assertThat(
                "Failed to get value from account storage",
                bar != null
        );
    }

    @Test
    public void testGetPassword() throws Exception {
        accountManager.setPassword(fakeAccount, "foo");
        String bar = accountStorage.get(AccountStorage.KEY);
        assertThat(
                "Failed to get password from account storage",
                bar != null
        );
    }

    @Test
    public void testRemove() throws Exception {
        accountManager.setUserData(fakeAccount, "foo", "bar");
        accountStorage.remove("foo");
        String foo = accountManager.getUserData(fakeAccount, "foo");
        assertThat(
                "Failed to remove value from account storage",
                foo == null
        );
    }


    @Test
    public void testRemovePassword() throws Exception {
        accountManager.setPassword(fakeAccount, "foo");
        accountStorage.remove(AccountStorage.KEY);
        String foo = accountManager.getPassword(fakeAccount);
        assertThat(
                "Failed to remove password from account storage",
                foo == null
        );
    }

    @Test
    public void testRemoveByKeys() throws Exception {
        accountManager.setUserData(fakeAccount, "foo", "bar");
        accountManager.setUserData(fakeAccount, "bar", "foo");
        accountStorage.remove("foo", "bar");
        String foo = accountManager.getUserData(fakeAccount, "foo");
        String bar = accountManager.getUserData(fakeAccount, "bar");
        assertThat(
                "Failed to remove list of values from account storage",
                foo == null && bar == null
        );
    }

    @Test
    public void testContains() throws Exception {
        accountManager.setUserData(fakeAccount, "foo", "bar");
        assertThat(
                "Contains 'foo' key condition failed",
                accountStorage.contains("foo")
        );
    }

    @Test
    public void testContainsPassword() throws Exception {
        accountManager.setPassword(fakeAccount, "foo");
        assertThat(
                "Contains password condition failed",
                accountStorage.contains(AccountStorage.KEY)
        );
    }
}