package com.jaspersoft.android.jaspermobile;

import android.content.Context;

import com.google.android.gms.security.ProviderInstaller;
import com.jaspersoft.android.jaspermobile.activities.SecurityProviderUpdater;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class JasperSecurityProviderUpdater implements SecurityProviderUpdater {

    @Override
    public void update(Context context, ProviderInstaller.ProviderInstallListener listener) {
        ProviderInstaller.installIfNeededAsync(context, listener);
    }
}
