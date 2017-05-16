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

package com.jaspersoft.android.jaspermobile.ui.mapper.job;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author Tom Koptel
 * @since 2.5
 */
enum SupportedLocales {
    INSTANCE;

    private final List<Locale> SUPPORTED_LOCALES = new LinkedList<>();

    SupportedLocales() {
        SUPPORTED_LOCALES.add(Locale.ENGLISH);
        SUPPORTED_LOCALES.add(Locale.GERMAN);
        SUPPORTED_LOCALES.add(new Locale("es"));
        SUPPORTED_LOCALES.add(Locale.FRENCH);
        SUPPORTED_LOCALES.add(new Locale("zh", "CN"));
        SUPPORTED_LOCALES.add(Locale.ITALIAN);
        SUPPORTED_LOCALES.add(Locale.JAPANESE);
        SUPPORTED_LOCALES.add(new Locale("pt", "BR"));
    }

    public Locale getCurrentLocale() {
        Locale currentLocale = Locale.getDefault();
        if (isSupported(currentLocale)) {
            return currentLocale;
        }
        return Locale.ENGLISH;
    }

    private boolean isSupported(Locale locale) {
        for (Locale supportedLocale : SUPPORTED_LOCALES) {
            if (!supportedLocale.getLanguage().equals(locale.getLanguage())) {
                continue;
            }
            if (supportedLocale.getCountry().isEmpty() || supportedLocale.getCountry().equals(locale.getCountry())) {
                return true;
            }
        }
        return false;
    }
}
