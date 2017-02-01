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

package com.jaspersoft.android.jaspermobile.util.cast;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.jaspersoft.android.jaspermobile.R;

import org.jetbrains.annotations.NotNull;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ResourceCastDialog extends MediaRouteControllerDialog implements ResourcePresentationService.ResourceCastStateCallback {
    private TextView resourceLabel;
    private ImageView resourceThumbnail;
    private ImageButton stopCastingResource;

    public ResourceCastDialog(Context context) {
        super(context, R.style.Theme_JasperMobile);
    }

    @Override
    public View onCreateMediaControlView(Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View castView = inflater.inflate(R.layout.dialog_resource_cast, null);

        resourceLabel = ((TextView) castView.findViewById(R.id.resourceLabel));
        resourceThumbnail = ((ImageView) castView.findViewById(R.id.resourceThumbnail));
        resourceThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        stopCastingResource = (ImageButton) castView.findViewById(R.id.stopCastResource);
        stopCastingResource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getResourcePresentationService().stopCasting();
            }
        });

        return castView;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        syncReportCastState();
        ResourcePresentationService.setResourceCastStateCallback(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ResourcePresentationService.setResourceCastStateCallback(null);
    }

    public void syncReportCastState(){
        ResourcePresentationService presentationService = getResourcePresentationService();
        String currentReport = presentationService.getCurrentReportName();
        if (currentReport == null) {
            resourceLabel.setText(R.string.cast_ready_message);
            stopCastingResource.setVisibility(View.GONE);
            resourceThumbnail.setVisibility(View.GONE);
        } else {
            resourceLabel.setText(currentReport);
            stopCastingResource.setVisibility(View.VISIBLE);
            Bitmap thumbnail = presentationService.getThumbnail();
            resourceThumbnail.setVisibility(thumbnail == null ? View.GONE : View.VISIBLE);
            resourceThumbnail.setImageBitmap(thumbnail == null ? null : thumbnail);
        }
    }

    @NotNull
    private ResourcePresentationService getResourcePresentationService() {
        CastRemoteDisplayLocalService localService = ResourcePresentationService.getInstance();
        if (localService == null) throw new IllegalStateException("Resource Presentation Service is null");

        return (ResourcePresentationService) ResourcePresentationService.getInstance();
    }

    @Override
    public void onCastStateChanged() {
        syncReportCastState();
    }
}
