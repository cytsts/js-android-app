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

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(JUnitParamsRunner.class)
public class SupportedLocalesTest {
    @Test
    @Parameters({
            "zh-TW",
            "pt-PT",
            "pt",
            "ko"
    })
    public void should_return_english_locale_if_current_not_supported(String languageTag) throws Exception {
        Locale locale = Locale.forLanguageTag(languageTag);
        Locale.setDefault(locale);
        Locale currentLocale = SupportedLocales.INSTANCE.getCurrentLocale();
        assertThat(currentLocale, is(Locale.ENGLISH));
    }

    @Test
    @Parameters({
            "en",
            "de",
            "es",
            "fr",
            "zh-CN",
            "it",
            "ja",
            "pt-BR",
            "en-CA",
            "fr-FR"
    })
    public void should_return_supported_locale(String languageTag) throws Exception {
        Locale locale = Locale.forLanguageTag(languageTag);
        Locale.setDefault(locale);
        Locale currentLocale = SupportedLocales.INSTANCE.getCurrentLocale();
        assertThat(currentLocale, is(locale));
    }
}