/*
 * Copyright � 2016 TIBCO Software,Inc.All rights reserved.
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

package com.jaspersoft.android.jaspermobile.data.mapper.job;

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobSimpleRecurrence;
import com.jaspersoft.android.sdk.service.data.schedule.RecurrenceIntervalUnit;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobScheduleFormRecurrenceUnitMapperTest {

    private JobScheduleFormRecurrenceUnitMapper unitMapper;

    JobSimpleRecurrence.Unit domainUnit = JobSimpleRecurrence.Unit.DAY;
    RecurrenceIntervalUnit dataUnit = RecurrenceIntervalUnit.DAY;

    @Before
    public void setUp() throws Exception {
        unitMapper = new JobScheduleFormRecurrenceUnitMapper();
    }

    @Test
    public void testToDataEntity() throws Exception {
        RecurrenceIntervalUnit unit = unitMapper.toDataEntity(domainUnit);
        assertThat(unit, is(dataUnit));
    }

    @Test
    public void testToDomainEntity() throws Exception {
        JobSimpleRecurrence.Unit unit = unitMapper.toDomainEntity(dataUnit);
        assertThat(unit, is(domainUnit));
    }
}