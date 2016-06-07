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

package com.jaspersoft.android.sdk.client.oxm.resource;

import com.google.gson.annotations.Expose;

/**
 * @author Tom Koptel
 * @since 1.10
 */
public class FileLookup extends ResourceLookup {
    @Expose
    protected String type;

    public void setFileType(String type) {
        this.type = type;
    }

    public FileType getFileType() {
        try {
            return FileType.valueOf(type);
        } catch (IllegalArgumentException ex) {
            return FileType.unknown;
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    public enum FileType {
        pdf,
        html,
        xls,
        rtf,
        csv,
        odt,
        txt,
        docx,
        ods,
        xlsx,
        font,
        img,
        jrxml,
        jar,
        prop,
        jrtx,
        xml,
        css,
        accessGrantSchema,
        olapMondrianSchema,
        json,
        pptx,
        unknown
    }
}