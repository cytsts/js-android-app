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

package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.ReportControlFlags;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.resource.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;
import com.jaspersoft.android.sdk.service.data.repository.Resource;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func2;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetReportShowControlsPropertyCase extends AbstractUseCase<ReportControlFlags, String> {
    private final ControlsRepository mControlsRepository;
    private final ResourceRepository mResourceRepository;

    @Inject
    public GetReportShowControlsPropertyCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ControlsRepository controlsRepository,
            ResourceRepository resourceRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mControlsRepository = controlsRepository;
        mResourceRepository = resourceRepository;
    }

    @Override
    protected Observable<ReportControlFlags> buildUseCaseObservable(@NonNull final String reportUri) {
        Observable<Resource> reportDetails = mResourceRepository.getResourceByType(reportUri, "reportUnit");
        Observable<List<InputControl>> listControls = mControlsRepository.listReportControls(reportUri);
        return reportDetails.zipWith(listControls, new Func2<Resource, List<InputControl>, ReportControlFlags>() {
            @Override
            public ReportControlFlags call(Resource resource, List<InputControl> controls) {
                ReportResource report = (ReportResource) resource;
                boolean hasControls = !controls.isEmpty();
                boolean needPrompt = report.alwaysPromptControls() && hasControls;
                return new ReportControlFlags(needPrompt, hasControls);
            }
        });
    }
}
