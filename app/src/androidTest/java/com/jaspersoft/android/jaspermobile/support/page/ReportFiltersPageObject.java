/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.support.page;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.withImageResource;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.AllOf.allOf;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
public class ReportFiltersPageObject extends PageObject {

    public void filtersListMatches(Matcher<View> listMatcher) {
        onView(withId(R.id.inputControlsList))
                .check(matches(listMatcher));
    }

    public void filterMatches(String filterName, Matcher<View> filterMatches) {
        scrollToItem(withText(containsString(filterName)));
        onView(allOf(hasSibling(withText(containsString(filterName))), withId(R.id.ic_value)))
                .check(matches(filterMatches));
    }

    public void valueSelected(String value) {
        scrollToItem(withText(containsString(value)));
        matchOn(value, withId(R.id.ic_boolean), isChecked());
    }

    public void hasItems(String... items){
        for (String item : items){
            scrollToItem(withText(containsString(item)));
            matchOn(item, withId(R.id.tvMultiSelectLabel), isDisplayed());
        }
    }

    public void crossButtonMatches(int resourceId) {
        onView(allOf(withImageResource(resourceId), withParent(withId(R.id.icToolbar))))
                .check(matches(isDisplayed()));
    }

    public void selectFilterValue(String filterValue) {
        scrollToItem(withText(containsString(filterValue)));
        clickOn(filterValue, withId(R.id.ic_boolean_title));
    }

    public void clickRunReport() {
        onView(withId(R.id.btnApplyParams))
                .perform(click());
    }

    public void clickSelected() {
        onView(withText(containsString("Selected:")))
                .perform(click());
    }

    public void editTextFilter(String name, String text) {
        onView(withText(startsWith(name)))
                .perform(click());
        onView(withId(R.id.icValue))
                .perform(replaceText(text));
        dialogPositiveButtonClick();
    }

    public void clickOnFilter(String name) {
        scrollToItem(withText(containsString(name)));
        clickOn(name, withId(R.id.ic_value));
    }

    private void scrollToItem(Matcher<View> matcher) {
        onView(allOf(withId(R.id.inputControlsList), isDisplayed())).
                perform(RecyclerViewActions.scrollTo(hasDescendant(matcher)));
    }

    private void clickOn(String value, Matcher<View> viewMatcher) {
        onView(allOf(hasSibling(withText(containsString(value))), viewMatcher))
                .perform(click());
    }

    private void matchOn(String value, Matcher<View> viewMatcher, Matcher<View> conditionMatcher) {
        onView(allOf(hasSibling(withText(containsString(value))), viewMatcher))
                .check(matches(conditionMatcher));
    }
}
