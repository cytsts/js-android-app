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

package com.jaspersoft.android.jaspermobile.domain.interactor.resource;

import com.jaspersoft.android.jaspermobile.domain.LoadFileRequest;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.resource.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.report.ResourceOutput;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class LoadResourceInFileCase extends AbstractUseCase<File, LoadFileRequest> {
    private final ResourceRepository mResourceRepository;

    @Inject
    public LoadResourceInFileCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ResourceRepository resourceRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mResourceRepository = resourceRepository;
    }

    @Override
    protected Observable<File> buildUseCaseObservable(final LoadFileRequest request) {
        return mResourceRepository.getResourceContent(request.getResourceUri())
                .flatMap(new Func1<ResourceOutput, Observable<File>>() {
                    @Override
                    public Observable<File> call(ResourceOutput resourceOutput) {
                        File target = request.getTarget();
                        File parentFolder = target.getParentFile();
                        if (parentFolder != null && !parentFolder.exists() && !parentFolder.mkdirs()) {
                            throw new IllegalStateException("Unable to create folder: " + parentFolder);
                        }

                        try {
                            OutputStream out = new FileOutputStream(target);
                            InputStream in = resourceOutput.getStream();
                            IOUtils.copy(in, out);
                        } catch (FileNotFoundException e) {
                           return Observable.error(e);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }

                        return Observable.just(target);
                    }
                });
    }
}
