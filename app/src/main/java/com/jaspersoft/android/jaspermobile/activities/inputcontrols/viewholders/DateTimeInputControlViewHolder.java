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

package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import com.jaspersoft.android.jaspermobile.util.IcDateHelper;
import com.jaspersoft.android.jaspermobile.widget.DateTimeView;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.Calendar;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class DateTimeInputControlViewHolder extends BaseInputControlViewHolder {

    private DateTimeView dateTimeView;

    public DateTimeInputControlViewHolder(DateTimeView itemView) {
        super(itemView);

        dateTimeView = itemView;
    }

    @Override
    public void populateView(InputControl inputControl) {
        dateTimeView.enableViews(!inputControl.isReadOnly());

        Calendar calendarDate = IcDateHelper.convertToDate(inputControl);
        dateTimeView.setDate(calendarDate);
        dateTimeView.setLabel(getUpdatedLabelText(inputControl));
        dateTimeView.setRequestCode(getAdapterPosition());

        showError(dateTimeView.getErrorView(), inputControl);
    }

    public void setDateTimeClickListener(DateTimeView.DateTimeClickListener dateTimeClickListener) {
        dateTimeView.setDateTimeClickListener(dateTimeClickListener);
    }
}
