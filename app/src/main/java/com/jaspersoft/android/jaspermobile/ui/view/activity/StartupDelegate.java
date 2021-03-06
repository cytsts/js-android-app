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

package com.jaspersoft.android.jaspermobile.ui.view.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.StartupActivityModule;
import com.jaspersoft.android.jaspermobile.ui.contract.StartupContract;
import com.jaspersoft.android.jaspermobile.ui.page.BasePageState;
import com.jaspersoft.android.jaspermobile.ui.presenter.StartupPresenter;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.ComponentProviderDelegate;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class StartupDelegate implements StartupContract.View {
    private static final int SIGN_UP_REQUEST_CODE = 1000;

    @Inject
    protected StartupPresenter mStartupPresenter;
    @Inject
    protected StartupContract.ActionListener mActionListener;
    @Inject
    protected Activity mActivity;

    private BasePageState mState;

    public StartupDelegate(FragmentActivity activity) {
        ComponentProviderDelegate.INSTANCE
                .getAppComponent(activity)
                .plus(new StartupActivityModule(activity))
                .inject(this);
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mState = new BasePageState();
        } else {
            mState = savedInstanceState.getParcelable("STATE");
        }

        mStartupPresenter.injectView(this);
        mActionListener.tryToSetupProfile(SIGN_UP_REQUEST_CODE);
    }

    @Override
    public BasePageState getState() {
        return mState;
    }

    public void onResume() {
        mStartupPresenter.resume();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("STATE", mState);
    }

    public void onDestroy() {
        mStartupPresenter.destroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGN_UP_REQUEST_CODE) {
            onSignUp(resultCode, data);
        }
    }

    private void onSignUp(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String profileName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Profile profile = Profile.create(profileName);
            mActionListener.setupNewProfile(profile);
        } else {
            mActivity.finish();
        }
    }
}
