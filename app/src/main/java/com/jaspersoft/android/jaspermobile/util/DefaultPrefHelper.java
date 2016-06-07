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

package com.jaspersoft.android.jaspermobile.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jaspersoft.android.jaspermobile.dialog.RateAppDialog;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.concurrent.TimeUnit;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean(scope = EBean.Scope.Singleton)
public class DefaultPrefHelper {
    public static final String KEY_PREF_REPO_CACHE_ENABLED = "pref_repo_cache_enabled";
    public static final String KEY_PREF_REPO_CACHE_EXPIRATION = "pref_repo_cache_expiration";
    public static final String KEY_PREF_CLEAR_CACHE = "pref_clear_cache";
    public static final String KEY_PREF_CONNECT_TIMEOUT = "pref_connect_timeout";
    public static final String KEY_PREF_READ_TIMEOUT = "pref_read_timeout";
    public static final String KEY_PREF_SEND_CRASHES = "pref_crash_reports";
    public static final String KEY_PREF_SCREEN_CAPTURING_ENABLED = "pref_screen_capturing_enabled";
    public static final String KEY_PREF_VOICE_COMMAND_HELP_ENABLED = "pref_voice_command_help_enabled";

    public static final boolean DEFAULT_REPO_CACHE_ENABLED = true;
    public static final String DEFAULT_REPO_CACHE_EXPIRATION = "48";
    public static final String DEFAULT_CONNECT_TIMEOUT = "15";
    public static final String DEFAULT_READ_TIMEOUT = "120";

    @RootContext
    Context context;

    public int getConnectTimeoutValue() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(
                KEY_PREF_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        return (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(value));
    }

    public int getReadTimeoutValue() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(
                KEY_PREF_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
        return (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(value));
    }

    public boolean isScreenCapturingEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(KEY_PREF_SCREEN_CAPTURING_ENABLED, false);
    }

    public boolean sendCrashReports() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(KEY_PREF_SEND_CRASHES, true);
    }

    public void setRepoCacheEnabled(boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(KEY_PREF_REPO_CACHE_ENABLED, value).apply();
    }

    public long getRepoCacheExpirationValue() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean repoCacheEnabled = preferences.getBoolean(KEY_PREF_REPO_CACHE_ENABLED, DEFAULT_REPO_CACHE_ENABLED);

        if (repoCacheEnabled) {
            String value = preferences.getString(KEY_PREF_REPO_CACHE_EXPIRATION, DEFAULT_REPO_CACHE_EXPIRATION);
            return Integer.parseInt(value) * TimeUnit.HOURS.toMillis(1);
        } else {
            return -1;
        }
    }

    public boolean isVoiceHelpDialogEnabled(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(KEY_PREF_VOICE_COMMAND_HELP_ENABLED, true);
    }

    public void setVoiceHelpDialogDisabled(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(KEY_PREF_VOICE_COMMAND_HELP_ENABLED, false).apply();
    }

    public void setRateDialogEnabled(boolean value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(
                RateAppDialog.KEY_PREF_NEED_TO_RATE, value).apply();
    }

    public long getLastRateTime(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(RateAppDialog.KEY_PREF_LAST_RATE_TIME, 0);
    }

    public void setLastRateTime(long value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(
                RateAppDialog.KEY_PREF_LAST_RATE_TIME, value).apply();
    }

    public long getNonRateLaunchCount(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(RateAppDialog.KEY_PREF_APP_LAUNCH_COUNT_WITHOUT_RATE, 0);
    }

    public void increaseNonRateLaunchCount(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long nonRateLaunchCount = getNonRateLaunchCount();
        nonRateLaunchCount = nonRateLaunchCount == RateAppDialog.LAUNCHES_UNTIL_SHOW ? nonRateLaunchCount : nonRateLaunchCount + 1;
        preferences.edit().putLong(
                RateAppDialog.KEY_PREF_APP_LAUNCH_COUNT_WITHOUT_RATE, nonRateLaunchCount).apply();
    }

    public void resetNonRateLaunchCount(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(
                RateAppDialog.KEY_PREF_APP_LAUNCH_COUNT_WITHOUT_RATE, 0).apply();
    }

    public boolean isRateDialogEnabled(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(RateAppDialog.KEY_PREF_NEED_TO_RATE, true);
    }
}
