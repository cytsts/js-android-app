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

package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class PageRequest {
    @NonNull
    private final String mUri;
    @NonNull
    private final String mRange;
    @NonNull
    private final String mFormat;

    private PageRequest(
            @NonNull String uri,
            @NonNull String range,
            @NonNull String format
    ) {
        mUri = uri;
        mRange = range;
        mFormat = format;
    }

    @NonNull
    public String getFormat() {
        return mFormat;
    }

    @NonNull
    public String getRange() {
        return mRange;
    }

    @NonNull
    public String getUri() {
        return mUri;
    }

    @NonNull
    public String getIdentifier() {
        return mUri + ":" + mRange + ":" + mFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageRequest request = (PageRequest) o;

        if (!mUri.equals(request.mUri)) return false;
        if (!mRange.equals(request.mRange)) return false;
        return mFormat.equals(request.mFormat);

    }

    @Override
    public int hashCode() {
        int result = mUri.hashCode();
        result = 31 * result + mRange.hashCode();
        result = 31 * result + mFormat.hashCode();
        return result;
    }

    public static class Builder {
        private String mUri;
        private String mRange;
        private String mFormat;

        public Builder setUri(String uri) {
            mUri = uri;
            return this;
        }

        public Builder setRange(String range) {
            mRange = range;
            return this;
        }

        public Builder asPdf() {
            mFormat = "PDF";
            return this;
        }

       public Builder asHtml() {
            mFormat = "HTML";
            return this;
        }

        public Builder setFormat(String format) {
            mFormat = format;
            return this;
        }

        public PageRequest build() {
            if (mFormat == null) {
                mFormat = "HTML";
            }
            if (mRange == null) {
                throw new NullPointerException("Range should not be null");
            }
            if (mUri == null) {
                throw new NullPointerException("Uri should not be null");
            }
            return new PageRequest(mUri, mRange, mFormat);
        }
    }
}
