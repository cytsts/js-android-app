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

package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPropertyCache;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.report.ReportMetadata;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryReportPropertyRepository implements ReportPropertyRepository {
    private final ReportPropertyCache mReportPropertyCache;
    private Observable<Integer> mGetTotalPagesCommand;

    @Inject
    public InMemoryReportPropertyRepository(
            ReportPropertyCache reportPropertyCache) {
        mReportPropertyCache = reportPropertyCache;
    }

    @NonNull
    @Override
    public Observable<Integer> getTotalPagesProperty(@NonNull final RxReportExecution reportExecution, @NonNull final String reportUri) {
        if (mGetTotalPagesCommand == null) {
            Observable<Integer> memorySource = Observable.defer(new Func0<Observable<Integer>>() {
                @Override
                public Observable<Integer> call() {
                    Integer totalPages = mReportPropertyCache.getTotalPages(reportUri);
                    if (totalPages == null) {
                        return Observable.empty();
                    }
                    return Observable.just(totalPages);
                }
            });
            Observable<Integer> networkSource = Observable.defer(new Func0<Observable<Integer>>() {
                @Override
                public Observable<Integer> call() {
                    return reportExecution
                            .waitForReportCompletion()
                            .map(new Func1<ReportMetadata, Integer>() {
                                @Override
                                public Integer call(ReportMetadata reportMetadata) {
                                    return reportMetadata.getTotalPages();
                                }
                            });
                }
            }).doOnNext(new Action1<Integer>() {
                @Override
                public void call(Integer pages) {
                    mReportPropertyCache.putTotalPages(reportUri, pages);
                }
            });

            mGetTotalPagesCommand = Observable.concat(memorySource, networkSource)
                    .first()
                    .cache()
                    .doOnTerminate(new Action0() {
                        @Override
                        public void call() {
                            mGetTotalPagesCommand = null;
                        }
                    });
        }
        return mGetTotalPagesCommand;
    }
}
