package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import com.jaspersoft.android.sdk.client.oxm.report.ReportDestination;
import com.jaspersoft.android.sdk.widget.report.renderer.Destination;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
public class DestinationMapper {

    @Inject
    public DestinationMapper() {
    }

    public Destination toDestination(ReportDestination reportDestination) {
        if (reportDestination.getPage() > 0) {
            return new Destination(reportDestination.getPage());
        }
        return new Destination(reportDestination.getAnchor());
    }

    public ReportDestination toReportDestination(Destination destination) {
        if (destination == null) return null;
        return new ReportDestination(destination.getAnchor(), destination.getPage() != null ? destination.getPage() : 0);
    }
}
