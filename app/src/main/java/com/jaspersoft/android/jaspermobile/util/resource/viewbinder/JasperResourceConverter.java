package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.accounts.Account;
import android.content.Context;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.resource.DashboardResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.ReportResource;
import com.jaspersoft.android.jaspermobile.util.resource.UndefinedResource;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperResourceConverter {

    private final boolean isAmberOrHigher;

    @Inject
    protected JsRestClient jsRestClient;

    public JasperResourceConverter(Context context) {
        RoboGuice.getInjector(context).injectMembersWithoutViews(this);

        Account account = JasperAccountManager.get(context).getActiveAccount();
        AccountServerData serverData = AccountServerData.get(context, account);
        ServerRelease serverRelease = ServerRelease.parseVersion(serverData.getVersionName());
        isAmberOrHigher = serverRelease.code() >= ServerRelease.AMBER.code();
    }

    public List<JasperResource> convertFromResourceLookups(List<ResourceLookup> listToConvert) {
        List<JasperResource> jasperResourceList = new ArrayList<>();
        if (listToConvert == null) return jasperResourceList;

        JasperResource resource;

        for (ResourceLookup resourceLookup : listToConvert) {
            switch (resourceLookup.getResourceType()) {
                case folder:
                    resource = new FolderResource(resourceLookup.getLabel(), resourceLookup.getDescription());
                    break;
                case legacyDashboard:
                case dashboard:
                    resource = new DashboardResource(resourceLookup.getLabel(), resourceLookup.getDescription());
                    break;
                case reportUnit:
                    String imageUri = "";
                    if (isAmberOrHigher) {
                        imageUri = jsRestClient.generateThumbNailUri(resourceLookup.getUri());
                    }
                    resource = new ReportResource(resourceLookup.getLabel(), resourceLookup.getDescription(), imageUri);
                    break;
                default:
                    resource = new UndefinedResource(resourceLookup.getLabel(), resourceLookup.getDescription());
                    break;
            }
            jasperResourceList.add(resource);
        }
        return jasperResourceList;
    }
}
