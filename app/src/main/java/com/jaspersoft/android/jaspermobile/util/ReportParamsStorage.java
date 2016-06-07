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

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Andrew Tivodar
 * @author Tom Koptel
 * @since 2.0
 */
@Singleton
public class ReportParamsStorage {
    private final Map<String, InputControlHolder> mInputControlHolderCache = new HashMap<>();

    @Inject
    public ReportParamsStorage() {
    }

    @NonNull
    public InputControlHolder getInputControlHolder(@NonNull String resourceUri) {
        return getInputControlHolderReference(resourceUri);
    }

    public void clearInputControlHolder(@NonNull String resourceUri) {
        mInputControlHolderCache.remove(resourceUri);
        mInputControlHolderCache.put(resourceUri, new InputControlHolder());
    }

    private InputControlHolder getInputControlHolderReference(String resourceUri) {
        InputControlHolder inputControlHolder = mInputControlHolderCache.get(resourceUri);
        if (inputControlHolder == null) {
            inputControlHolder = new InputControlHolder();
            mInputControlHolderCache.put(resourceUri, inputControlHolder);
        }
        return inputControlHolder;
    }
}
