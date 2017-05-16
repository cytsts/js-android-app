package com.jaspersoft.android.sdk.client.oxm.report;

import java.io.Serializable;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
public class ReportDestination implements Serializable{
    private final String anchor;
    private final int page;

    public ReportDestination(String anchor, int page) {
        this.anchor = anchor;
        this.page = page;
    }

    public String getAnchor() {
        return anchor;
    }

    public int getPage() {
        return page;
    }
}
