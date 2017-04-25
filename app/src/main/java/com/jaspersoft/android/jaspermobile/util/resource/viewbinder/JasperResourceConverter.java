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

package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.data.ThumbNailGenerator;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.util.resource.DashboardResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.JobResource;
import com.jaspersoft.android.jaspermobile.util.resource.LegacyDashboardResource;
import com.jaspersoft.android.jaspermobile.util.resource.ReportResource;
import com.jaspersoft.android.jaspermobile.util.resource.SavedItemResource;
import com.jaspersoft.android.jaspermobile.util.resource.UndefinedResource;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.schedule.JobState;
import com.jaspersoft.android.sdk.service.data.schedule.JobUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
@PerProfile
public class JasperResourceConverter {

    private final ThumbNailGenerator mThumbNailGenerator;

    @Inject
    public JasperResourceConverter(ThumbNailGenerator thumbNailGenerator) {
        mThumbNailGenerator = thumbNailGenerator;
    }

    public JasperResource convertToJasperResource(ResourceLookup resourceLookup) {
        JasperResource resource;
        switch (resourceLookup.getResourceType()) {
            case folder:
                resource = new FolderResource(resourceLookup.getUri(), resourceLookup.getLabel(), resourceLookup.getDescription());
                break;
            case legacyDashboard:
                resource = new LegacyDashboardResource(resourceLookup.getUri(), resourceLookup.getLabel(), resourceLookup.getDescription());
                break;
            case dashboard:
                resource = new DashboardResource(resourceLookup.getUri(), resourceLookup.getLabel(), resourceLookup.getDescription());
                break;
            case reportUnit:
                String imageUri = mThumbNailGenerator.generate(resourceLookup.getUri());
                resource = new ReportResource(resourceLookup.getUri(), resourceLookup.getLabel(), resourceLookup.getDescription(), imageUri);
                break;
            case file:
                resource = new FileResource(resourceLookup.getUri(), resourceLookup.getLabel(), resourceLookup.getDescription(), resourceLookup.getUri());
                break;
            default:
                resource = new UndefinedResource(resourceLookup.getUri(), resourceLookup.getLabel(), resourceLookup.getDescription());
                break;
        }
        // TODO: add adhoc data view resource
        return resource;
    }

    public List<JasperResource> convertToJasperResource(List<ResourceLookup> listToConvert) {
        List<JasperResource> jasperResourceList = new ArrayList<>();
        if (listToConvert == null) return jasperResourceList;

        for (ResourceLookup resourceLookup : listToConvert) {
            JasperResource resource = convertToJasperResource(resourceLookup);
            jasperResourceList.add(resource);
        }
        return jasperResourceList;
    }

    public List<JasperResource> convertToJasperResources(Context context, List<JobUnit> jobsList) {
        List<JasperResource> jasperResourceList = new ArrayList<>();
        if (jobsList == null) return jasperResourceList;

        for (JobUnit job : jobsList) {
            JasperResource jasperResource = new JobResource(String.valueOf(job.getId()), job.getLabel(),
                    job.getDescription(), job.getNextFireTime(), parseJobState(context, job.getState()));
            jasperResourceList.add(jasperResource);
        }
        return jasperResourceList;
    }

    public ResourceLookup.ResourceType convertToResourceType(JasperResource resource) {
        if (resource == null) return null;

        ResourceLookup.ResourceType resourceType;
        switch (resource.getResourceType()) {
            case folder:
                resourceType = ResourceLookup.ResourceType.folder;
                break;
            case legacyDashboard:
                resourceType = ResourceLookup.ResourceType.legacyDashboard;
                break;
            case dashboard:
                resourceType = ResourceLookup.ResourceType.dashboard;
                break;
            case report:
                resourceType = ResourceLookup.ResourceType.reportUnit;
                break;
            case file:
                resourceType = ResourceLookup.ResourceType.file;
                break;
            default:
                resourceType = ResourceLookup.ResourceType.unknown;
                break;
        }
        return resourceType;
    }

    public List<JasperResource> convertToJasperResource(Cursor cursor, String tableId, Uri tableContentUri) {
        List<JasperResource> jasperResourceList = new ArrayList<>();
        if (cursor == null) return jasperResourceList;

        if (cursor.moveToFirst()) {
            do {
                ResourceLookup resourceLookup = convertFromFavoritesCursorToLookup(cursor);
                JasperResource resource;

                String id = cursor.getString(cursor.getColumnIndex(tableId));
                String entryUri = Uri.withAppendedPath(tableContentUri, id).toString();

                switch (resourceLookup.getResourceType()) {
                    case folder:
                        resource = new FolderResource(entryUri, resourceLookup.getLabel(), resourceLookup.getDescription());
                        break;
                    case legacyDashboard:
                    case dashboard:
                        resource = new DashboardResource(entryUri, resourceLookup.getLabel(), resourceLookup.getDescription());
                        break;
                    case file:
                        resource = new FileResource(entryUri, resourceLookup.getLabel(), resourceLookup.getDescription(), resourceLookup.getUri());
                        break;
                    case reportUnit:
                        String imageUri = mThumbNailGenerator.generate(resourceLookup.getUri());
                        resource = new ReportResource(entryUri, resourceLookup.getLabel(), resourceLookup.getDescription(), imageUri);
                        break;
                    default:
                        resource = new UndefinedResource(entryUri, resourceLookup.getLabel(), resourceLookup.getDescription());
                        break;
                }
                jasperResourceList.add(resource);
            } while (cursor.moveToNext());
        }
        return jasperResourceList;
    }

    public List<JasperResource> convertToJasperResource(Cursor cursor) {
        List<JasperResource> jasperResourceList = new ArrayList<>();
        if (cursor == null) return jasperResourceList;

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(SavedItemsTable._ID));
                String entryUri = Uri.withAppendedPath(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI, id).toString();
                String fileName = cursor.getString(cursor.getColumnIndex(SavedItemsTable.NAME));
                String fileDescription = cursor.getString(cursor.getColumnIndex(SavedItemsTable.DESCRIPTION));
                String fileFormat = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_FORMAT));
                SavedItemResource.FileType fileType = SavedItemResource.FileType.getValueOf(fileFormat);
                boolean downloaded = cursor.getInt(cursor.getColumnIndex(SavedItemsTable.DOWNLOADED)) == 1;

                JasperResource resource = new SavedItemResource(entryUri, fileName, fileDescription, fileType, downloaded);
                jasperResourceList.add(resource);
            } while (cursor.moveToNext());
        }
        return jasperResourceList;
    }

    public HashMap<String, ResourceLookup> convertToDataMap(List<ResourceLookup> listToConvert) {
        HashMap<String, ResourceLookup> jasperResourceMap = new HashMap<>();
        if (listToConvert == null) return jasperResourceMap;

        for (ResourceLookup resourceLookup : listToConvert) {
            jasperResourceMap.put(resourceLookup.getUri(), resourceLookup);
        }
        return jasperResourceMap;
    }

    public ResourceLookup convertFavoriteToResourceLookup(String id, Context context) {
        Cursor cursor = context.getContentResolver().query(Uri.parse(id), null, null, null, null);
        cursor.moveToFirst();
        ResourceLookup resourceLookup = convertFromFavoritesCursorToLookup(cursor);
        cursor.close();
        return resourceLookup;
    }

    public File convertToFile(String id, Context context) {
        Cursor cursor = context.getContentResolver().query(Uri.parse(id), null, null, null, null);
        cursor.moveToFirst();
        File file = new File(cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_PATH)));
        cursor.close();
        return file;
    }

    private ResourceLookup convertFromFavoritesCursorToLookup(Cursor cursor) {
        ResourceLookup resource = new ResourceLookup();
        resource.setLabel(cursor.getString(cursor.getColumnIndex(FavoritesTable.TITLE)));
        resource.setDescription(cursor.getString(cursor.getColumnIndex(FavoritesTable.DESCRIPTION)));
        resource.setUri(cursor.getString(cursor.getColumnIndex(FavoritesTable.URI)));
        resource.setResourceType(cursor.getString(cursor.getColumnIndex(FavoritesTable.WSTYPE)));
        resource.setCreationDate(cursor.getString(cursor.getColumnIndex(FavoritesTable.CREATION_TIME)));

        return resource;
    }

    private String parseJobState(Context context, JobState jobState) {
        switch (jobState) {
            case NORMAL:
                return context.getString(R.string.sch_state_normal);
            case COMPLETE:
                return context.getString(R.string.sch_state_complete);
            case EXECUTING:
                return context.getString(R.string.sch_state_executing);
            case ERROR:
                return context.getString(R.string.sch_state_error);
            case PAUSED:
                return context.getString(R.string.sch_state_paused);
            default:
                return context.getString(R.string.sch_state_unknown);
        }
    }
}
