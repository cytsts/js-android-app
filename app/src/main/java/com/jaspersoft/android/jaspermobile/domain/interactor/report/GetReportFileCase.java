/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetReportFileCase extends AbstractUseCase<File, PageRequest> {
    private final ReportRepository mReportRepository;
    private final ReportPageRepository mReportPageRepository;
    private final Context context;

    @Inject
    public GetReportFileCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ReportRepository reportRepository,
            ReportPageRepository reportPageRepository,
            @ApplicationContext
            Context context) {
        super(preExecutionThread, postExecutionThread);
        mReportRepository = reportRepository;
        mReportPageRepository = reportPageRepository;
        this.context = context;
    }

    @Override
    protected Observable<File> buildUseCaseObservable(final PageRequest pageRequest) {
        return mReportRepository.getReport(pageRequest.getUri())
                .flatMap(new Func1<RxReportExecution, Observable<ReportPage>>() {
                    @Override
                    public Observable<ReportPage> call(RxReportExecution execution) {
                        return mReportPageRepository.get(execution, pageRequest);
                    }
                }).flatMap(new Func1<ReportPage, Observable<File>>() {
                    @Override
                    public Observable<File> call(ReportPage page) {
                        File reportFile = getReportFileFile();

                        try {
                            OutputStream output = new FileOutputStream(reportFile);
                            InputStream stream = new ByteArrayInputStream(page.getContent());
                            IOUtils.copy(stream, output);
                            IOUtils.closeQuietly(stream);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }

                        return Observable.just(reportFile);
                    }
                });
    }


    private File getReportFileFile() {
        File cacheDir = context.getExternalCacheDir();
        File resourceCacheDir = new File(cacheDir, "report_dir_cache");

        if (!resourceCacheDir.exists() && !resourceCacheDir.mkdirs()) {
            Timber.e("Unable to create %s", resourceCacheDir);
            return null;
        }
        return new File(resourceCacheDir, "reportFile");
    }
}
