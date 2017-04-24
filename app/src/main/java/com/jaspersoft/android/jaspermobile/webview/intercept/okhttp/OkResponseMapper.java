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

package com.jaspersoft.android.jaspermobile.webview.intercept.okhttp;

import com.jaspersoft.android.jaspermobile.webview.WebResponse;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Tom Koptel
 * @since 2.5
 */
class OkResponseMapper {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_ENCODING = "Content-Encoding";

    public WebResponse toWebViewResponse(Response response) {
        final String mimeType = response.header(CONTENT_TYPE);
        final String encoding = response.header(CONTENT_ENCODING);
        final InputStream data = extractData(response);
        final int statusCode = response.code();
        final String reasonPhrase = response.message();
        final Map<String, String> responseHeaders = extractHeaders(response);

        return new WebResponse() {
            @Override
            public String getMimeType() {
                return mimeType;
            }

            @Override
            public String getEncoding() {
                return encoding;
            }

            @Override
            public InputStream getData() {
                return data;
            }

            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public String getReasonPhrase() {
                //TODO: find another solution for the case
                if (reasonPhrase.isEmpty()) {
                    if (statusCode == 200) {
                        return "OK";
                    }
                }
                return reasonPhrase;
            }

            @Override
            public Map<String, String> getResponseHeaders() {
                return responseHeaders;
            }
        };
    }

    private InputStream extractData(Response response) {
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        }
        try {
            return body.byteStream();
        } catch (IOException e) {
            return null;
        }
    }

    private Map<String, String> extractHeaders(Response response) {
        Map<String, List<String>> headers = response.headers().toMultimap();
        Map<String, String> extractedHeaders = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            List<String> value = entry.getValue();
            int size = value.size();
            extractedHeaders.put(entry.getKey(), value.get(size - 1));
        }
        return extractedHeaders;
    }
}
