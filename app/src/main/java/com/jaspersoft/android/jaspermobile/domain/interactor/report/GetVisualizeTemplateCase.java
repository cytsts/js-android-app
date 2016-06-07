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

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.VisualizeTemplateRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import java.util.Map;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetVisualizeTemplateCase extends AbstractUseCase<VisualizeTemplate, Map<String, ?>> {

    private final VisualizeTemplateRepository mVisualizeTemplateRepository;
    private final Profile mProfile;

    @Inject
    public GetVisualizeTemplateCase(PreExecutionThread preExecutionThread,
                                    PostExecutionThread postExecutionThread,
                                    VisualizeTemplateRepository visualizeTemplateRepository,
                                    Profile profile) {
        super(preExecutionThread, postExecutionThread);
        mVisualizeTemplateRepository = visualizeTemplateRepository;
        mProfile = profile;
    }

    @Override
    protected Observable<VisualizeTemplate> buildUseCaseObservable(Map<String, ?> clientParams) {
        return mVisualizeTemplateRepository.get(mProfile, clientParams);
    }
}
