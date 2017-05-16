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

package com.jaspersoft.android.jaspermobile.data;

import com.jaspersoft.android.jaspermobile.data.cache.profile.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.JasperServerCache;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.sdk.network.AuthenticationLifecycle;
import com.jaspersoft.android.sdk.network.AuthorizedClient;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.network.SpringCredentials;
import com.jaspersoft.android.sdk.service.dashboard.DashboardService;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.exception.StatusCodes;
import com.jaspersoft.android.sdk.service.filter.FiltersService;
import com.jaspersoft.android.sdk.service.report.ReportService;
import com.jaspersoft.android.sdk.service.report.schedule.ReportScheduleService;
import com.jaspersoft.android.sdk.service.repository.RepositoryService;
import com.jaspersoft.android.sdk.service.rx.filter.RxFiltersService;
import com.jaspersoft.android.sdk.service.rx.report.RxReportService;
import com.jaspersoft.android.sdk.service.rx.report.schedule.RxReportScheduleService;
import com.jaspersoft.android.sdk.service.rx.repository.RxRepositoryService;

import java.net.CookieHandler;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class RealJasperRestClient implements JasperRestClient {
    private final Server.Builder serverBuilder;
    private final CookieHandler cookieHandler;
    private final Profile profile;
    private final CredentialsCache credentialsCache;
    private final JasperServerCache serverCache;
    private final AuthenticationLifecycle authenticationHandler;

    public RealJasperRestClient(
            Server.Builder serverBuilder,
            CookieHandler cookieHandler,
            AuthenticationLifecycle authenticationHandler,
            Profile profile,
            CredentialsCache credentialsCache,
            JasperServerCache serverCache
    ) {
        this.serverBuilder = serverBuilder;
        this.cookieHandler = cookieHandler;
        this.profile = profile;
        this.credentialsCache = credentialsCache;
        this.serverCache = serverCache;
        this.authenticationHandler = authenticationHandler;
    }

    @Override
    public AuthorizedClient authorizedClient() {
        return createAuthorizedClient().toBlocking().first();
    }

    @Override
    public ReportService syncReportService() {
        AuthorizedClient client = createAuthorizedClient().toBlocking().first();
        return ReportService.newService(client);
    }

    @Override
    public DashboardService syncDashboardService() {
        AuthorizedClient client = createAuthorizedClient().toBlocking().first();
        return DashboardService.newService(client);
    }

    @Override
    public FiltersService syncFilterService() {
        AuthorizedClient client = createAuthorizedClient().toBlocking().first();
        return FiltersService.newService(client);
    }

    @Override
    public RepositoryService syncRepositoryService() {
        AuthorizedClient client = createAuthorizedClient().toBlocking().first();
        return RepositoryService.newService(client);
    }

    @Override
    public ReportScheduleService syncScheduleService() {
        AuthorizedClient client = createAuthorizedClient().toBlocking().first();
        return ReportScheduleService.newService(client);
    }

    @Override
    public Observable<RxReportService> reportService() {
        return createAuthorizedClient().map(new Func1<AuthorizedClient, RxReportService>() {
            @Override
            public RxReportService call(AuthorizedClient authorizedClient) {
                return RxReportService.newService(authorizedClient);
            }
        });
    }

    @Override
    public Observable<RxRepositoryService> repositoryService() {
        return createAuthorizedClient().map(new Func1<AuthorizedClient, RxRepositoryService>() {
            @Override
            public RxRepositoryService call(AuthorizedClient authorizedClient) {
                return RxRepositoryService.newService(authorizedClient);
            }
        });
    }

    @Override
    public Observable<RxFiltersService> filtersService() {
        return createAuthorizedClient().map(new Func1<AuthorizedClient, RxFiltersService>() {
            @Override
            public RxFiltersService call(AuthorizedClient authorizedClient) {
                return RxFiltersService.newService(authorizedClient);
            }
        });
    }

    @Override
    public Observable<RxReportScheduleService> scheduleService() {
        return createAuthorizedClient().map(new Func1<AuthorizedClient, RxReportScheduleService>() {
            @Override
            public RxReportScheduleService call(AuthorizedClient authorizedClient) {
                return RxReportScheduleService.newService(authorizedClient);
            }
        });
    }

    private Observable<AuthorizedClient> createAuthorizedClient() {
        return Observable.defer(new Func0<Observable<AuthorizedClient>>() {
            @Override
            public Observable<AuthorizedClient> call() {
                AppCredentials appCredentials = credentialsCache.get(profile);
                boolean passwordMissing = AppCredentials.NO_PASSWORD.equals(appCredentials.getPassword());
                if (passwordMissing) {
                    ServiceException serviceException =
                            new ServiceException("User is not authorized", null, StatusCodes.AUTHORIZATION_ERROR);
                    return Observable.error(serviceException);
                }

                Credentials credentials = SpringCredentials.builder()
                        .withUsername(appCredentials.getUsername())
                        .withPassword(appCredentials.getPassword())
                        .withOrganization(appCredentials.getOrganization())
                        .build();

                AuthorizedClient authorizedClient = provideServer().newClient(credentials)
                        .withCookieHandler(cookieHandler)
                        .withAuthenticationLifecycle(authenticationHandler)
                        .create();
                return Observable.just(authorizedClient);
            }
        });
    }

    private Server provideServer() {
        JasperServer server = providesJasperServer();
        return serverBuilder
                .withBaseUrl(server.getBaseUrl())
                .build();
    }

    private JasperServer providesJasperServer() {
        return serverCache.get(profile);
    }
}
