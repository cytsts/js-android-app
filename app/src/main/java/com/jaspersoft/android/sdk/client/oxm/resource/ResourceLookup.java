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

package com.jaspersoft.android.sdk.client.oxm.resource;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

/**
 * This class represents a resource lookup entity for convenient XML serialization process.
 *
 * @author Ivan Gadzhega
 * @since 1.7
 */
public class ResourceLookup implements Parcelable {

    @Expose
    protected String label;
    @Expose
    protected String description;
    @Expose
    protected String uri;
    @Expose
    protected String resourceType;

    @Expose
    protected int version;
    @Expose
    protected int permissionMask;
    @Expose
    protected String creationDate;
    @Expose
    protected String updateDate;

    public ResourceLookup() { }

    //---------------------------------------------------------------------
    // Parcelable
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // Getters & Setters
    //---------------------------------------------------------------------

    public ResourceType getResourceType() {
        try {
            return ResourceType.valueOf(resourceType);
        } catch (IllegalArgumentException ex) {
            return ResourceType.unknown;
        }
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType.toString();
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getPermissionMask() {
        return permissionMask;
    }

    public void setPermissionMask(int permissionMask) {
        this.permissionMask = permissionMask;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    public enum ResourceType {
        folder,
        reportUnit,
        dashboard,
        legacyDashboard,
        file,
        adhocDataView,
        unknown
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.label);
        dest.writeString(this.description);
        dest.writeString(this.uri);
        dest.writeString(this.resourceType);
        dest.writeInt(this.version);
        dest.writeInt(this.permissionMask);
        dest.writeString(this.creationDate);
        dest.writeString(this.updateDate);
    }

    protected ResourceLookup(Parcel in) {
        this.label = in.readString();
        this.description = in.readString();
        this.uri = in.readString();
        this.resourceType = in.readString();
        this.version = in.readInt();
        this.permissionMask = in.readInt();
        this.creationDate = in.readString();
        this.updateDate = in.readString();
    }

    public static final Creator<ResourceLookup> CREATOR = new Creator<ResourceLookup>() {
        public ResourceLookup createFromParcel(Parcel source) {
            return new ResourceLookup(source);
        }

        public ResourceLookup[] newArray(int size) {
            return new ResourceLookup[size];
        }
    };
}
