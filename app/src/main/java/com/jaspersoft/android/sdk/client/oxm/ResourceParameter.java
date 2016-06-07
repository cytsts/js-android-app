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

package com.jaspersoft.android.sdk.client.oxm;

import com.google.gson.annotations.Expose;

/**
 * This class represents a resource parameter entity for convenient XML serialization process.
 *
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class ResourceParameter {

    @Expose
    private String name;

    @Expose
    private boolean isListItem;

    @Expose
    private String value;

    public ResourceParameter() { }

    public ResourceParameter(String name, String value) {
        this(name, value, false);
    }

    public ResourceParameter(String name, boolean value, boolean isListItem) {
        this(name, Boolean.toString(value), isListItem);
    }

    public ResourceParameter(String name, String value, boolean isListItem) {
        setName(name);
        setValue(value);
        isListItem(isListItem);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isListItem() {
        return isListItem;
    }

    public void isListItem(boolean listItem) {
        isListItem = listItem;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
