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

package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.ThumbNailGenerator;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.util.resource.DashboardResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.LegacyDashboardResource;
import com.jaspersoft.android.jaspermobile.util.resource.ReportResource;
import com.jaspersoft.android.jaspermobile.util.resource.UndefinedResource;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.FolderResource;
import com.jaspersoft.android.sdk.client.oxm.report.FolderDataResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.FileLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.service.data.report.FileResource;
import com.jaspersoft.android.sdk.service.data.repository.Resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class ResourceMapper {
    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final ThumbNailGenerator mThumbNailGenerator;

    @Inject
    public ResourceMapper(ThumbNailGenerator thumbNailGenerator) {
        mThumbNailGenerator = thumbNailGenerator;
    }

    @NonNull
    public List<JasperResource> toJasperResources(@NonNull List<Resource> resources) {
        List<JasperResource> list = new ArrayList<>(resources.size());
        for (Resource resource : resources) {
            if (resource != null) {
                JasperResource jasperResource = toJasperResource(resource);
                list.add(jasperResource);
            }
        }
        return list;
    }

    @NonNull
    public List<FolderDataResponse> toLegacyFolders(@NonNull List<Resource> resources) {
        List<FolderDataResponse> list = new ArrayList<>(resources.size());
        for (Resource resource : resources) {
            if (resource != null) {
                FolderDataResponse folder = new FolderDataResponse();
                toLegacyResource(resource, folder);
                list.add(folder);
            }
        }
        return list;
    }

    @NonNull
    public List<ResourceLookup> toLegacyResources(@NonNull List<Resource> resources, ResourceLookupSearchCriteria criteria) {
        List<ResourceLookup> list = new ArrayList<>(resources.size());
        for (Resource resource : resources) {
            if (resource != null) {
                ResourceLookup lookup = new ResourceLookup();
                lookup = toLegacyResource(resource, lookup);
                if (lookup != null && criteria.getTypes().contains(lookup.getResourceType().name())) {
                    list.add(lookup);
                }
            }
        }
        return list;
    }

    public ResourceLookup toConcreteLegacyResource(@NonNull Resource resource, @NonNull String type)
            throws Exception {
        ResourceLookup lookup = new ResourceLookup();
        if ("file".equals(type)) {
            FileResource fileResource = (FileResource) resource;
            FileResource.Type fileType = fileResource.getType();
            String legacyFileType = fileType.name();

            FileLookup fileLookup = new FileLookup();
            fileLookup.setFileType(legacyFileType);
            lookup = fileLookup;
        }
        toLegacyResource(resource, lookup);
        return lookup;
    }

    public ResourceLookup toLegacyResource(@NonNull Resource resource, @NonNull ResourceLookup lookup) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault());
        String creationDate = simpleDateFormat.format(resource.getCreationDate());
        String updateDate = simpleDateFormat.format(resource.getUpdateDate());

        String rawValue = resource.getResourceType().getRawValue();
        try {
            ResourceLookup.ResourceType resourceType = ResourceLookup.ResourceType.valueOf(rawValue);
            lookup.setResourceType(resourceType);

            lookup.setLabel(resource.getLabel());
            lookup.setDescription(resource.getDescription());
            lookup.setUri(resource.getUri());
            lookup.setVersion(resource.getVersion());
            lookup.setCreationDate(creationDate);
            lookup.setUpdateDate(updateDate);
            lookup.setPermissionMask(resource.getPermissionMask().getMask());
        } catch (IllegalArgumentException e) {
            lookup = null;
        }

        return lookup;
    }

    private JasperResource toJasperResource(Resource resource) {
        JasperResource jasperresource;
        switch (resource.getResourceType()) {
            case folder:
                jasperresource = new FolderResource(resource.getUri(), resource.getLabel(), resource.getDescription());
                break;
            case legacyDashboard:
                jasperresource = new LegacyDashboardResource(resource.getUri(), resource.getLabel(), resource.getDescription());
                break;
            case dashboard:
                jasperresource = new DashboardResource(resource.getUri(), resource.getLabel(), resource.getDescription());
                break;
            case reportUnit:
                String imageUri = mThumbNailGenerator.generate(resource.getUri());
                jasperresource = new ReportResource(resource.getUri(), resource.getLabel(), resource.getDescription(), imageUri);
                break;
            case file:
                jasperresource = new com.jaspersoft.android.jaspermobile.util.resource.viewbinder.FileResource(resource.getUri(), resource.getLabel(), resource.getDescription(), resource.getUri());
                break;
            default:
                jasperresource = new UndefinedResource(resource.getUri(), resource.getLabel(), resource.getDescription());
                break;
        }
        return jasperresource;
    }
}
