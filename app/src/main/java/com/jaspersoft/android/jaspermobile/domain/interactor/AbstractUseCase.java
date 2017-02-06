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

package com.jaspersoft.android.jaspermobile.domain.interactor;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class AbstractUseCase<Result, Argument> implements UseCase<Result, Argument> {
    private Subscription subscription = Subscriptions.empty();
    private Subscriber<? super Result> useCaseSubscriber;

    /**
     * Builds an {@link rx.Observable} which will be used when executing the current {@link AbstractSimpleUseCase}.
     */
    protected abstract Observable<Result> buildUseCaseObservable(Argument argument);

    private final PreExecutionThread mPreExecutionThread;
    private final PostExecutionThread mPostExecutionThread;

    protected AbstractUseCase(PreExecutionThread preExecutionThread, PostExecutionThread postExecutionThread) {
        mPreExecutionThread = preExecutionThread;
        mPostExecutionThread = postExecutionThread;
    }

    /**
     * Executes the current use case.
     *
     * @param useCaseSubscriber The guy who will be listen to the observable build with {@link #buildUseCaseObservable(Argument)}.
     */
    @Override
    public Subscription execute(@NonNull Argument argument, @NonNull Subscriber<? super Result> useCaseSubscriber) {
        Observable<Result> command = this.buildUseCaseObservable(argument);
        this.useCaseSubscriber = useCaseSubscriber;
        this.subscription = command
                .subscribeOn(mPreExecutionThread.getScheduler())
                .observeOn(mPostExecutionThread.getScheduler())
                .subscribe(new InternalSubscriber());
        return subscription;
    }

    public Result execute(@NonNull Argument argument) {
        Observable<Result> command = this.buildUseCaseObservable(argument);
        return command.toBlocking().firstOrDefault(null);
    }

    public void updateSubscriber(@NonNull Subscriber<? super Result> useCaseSubscriber) {
        this.useCaseSubscriber = useCaseSubscriber;
    }

    /**
     * Unsubscribes from current {@link rx.Subscription}.
     */
    @Override
    public void unsubscribe() {
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        useCaseSubscriber = null;
    }

    private class InternalSubscriber extends Subscriber<Result> {
        @Override
        public void onStart() {
            if (useCaseSubscriber != null) {
                useCaseSubscriber.onStart();
            }
        }

        @Override
        public void onCompleted() {
            if (useCaseSubscriber != null) {
                useCaseSubscriber.onCompleted();
            }
        }

        @Override
        public void onError(Throwable e) {
            if (useCaseSubscriber != null) {
                useCaseSubscriber.onError(e);
            }
        }

        @Override
        public void onNext(Result result) {
            if (useCaseSubscriber != null) {
                useCaseSubscriber.onNext(result);
            }
        }
    }
}
