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

package com.jaspersoft.android.jaspermobile.data.cache.profile;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.Profile;

import java.util.List;

/**
 * Abstraction around profile cache
 * <br/>
 * Following interface implemented by {@link AccountProfileCache}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface ProfileCache {
    /**
     * Saves profile in cache.
     *
     * @param profile profile we are going to persist
     * @return flag that notifies client about success or fail during persist operation
     */
    Profile put(@NonNull Profile profile);

    /**
     * Notifies either profile in cache or not.
     *
     * @param profile profile we are check for existence in cache
     * @return flag that speaks about either profile in cache or not
     */
    boolean hasProfile(@NonNull Profile profile);

    List<Profile> getAll();
}
