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

package com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.BaseInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.BooleanInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.DateTimeInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.MultiSelectInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.SingleSelectInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.TextInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.ValueInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.widget.DateTimeView;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class InputControlsAdapter extends RecyclerView.Adapter<BaseInputControlViewHolder> {

    private final static int IC_UNSUPPORTED = -1;
    private final static int IC_BOOLEAN = 0;
    private final static int IC_VALUE = 1;
    private final static int IC_NUMBER_VALUE = 2;
    private final static int IC_DATE_TIME = 3;
    private final static int IC_DATE = 4;
    private final static int IC_TIME = 5;
    private final static int IC_SINGLE_SELECT = 6;
    private final static int IC_MULTI_SELECT = 7;

    private List<InputControl> mInputControls;
    private boolean mIsBinding;
    private LayoutInflater mLayoutInflater;
    private InputControlInteractionListener mInteractionListener;

    public InputControlsAdapter(List<InputControl> inputControls) {
        if (inputControls == null) {
            throw new IllegalArgumentException("Input Controls can not be null!");
        }

        updateInputControlList(inputControls);
    }

    public void setInteractionListener(InputControlInteractionListener interactionListener) {
        this.mInteractionListener = interactionListener;
    }

    public void updateInputControlList(List<InputControl> inputControls) {
        mInputControls = new ArrayList<>();
        for (InputControl inputControl : inputControls) {
            if (inputControl.isVisible()) {
                mInputControls.add(inputControl);
            }
        }
        notifyItemRangeChanged(0, mInputControls.size());
    }

    public void updateInputControl(InputControl inputControl){
        int position = mInputControls.indexOf(inputControl);
        if (position != -1) {
            hideError(position);
            notifyItemChanged(position);
        }
    }

    @Override
    public BaseInputControlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem;
        switch (viewType) {
            case IC_BOOLEAN:
                listItem = layoutInflater.inflate(R.layout.item_input_control_boolean, parent, false);
                BooleanInputControlViewHolder booleanInputControlViewHolder = new BooleanInputControlViewHolder(listItem);
                booleanInputControlViewHolder.setStateChangeListener(new BooleanIcInteractionListener());
                return booleanInputControlViewHolder;
            case IC_VALUE:
            case IC_NUMBER_VALUE:
                listItem = layoutInflater.inflate(R.layout.item_input_control_value, parent, false);
                TextInputControlViewHolder textInputControlViewHolder = new TextInputControlViewHolder(listItem);
                textInputControlViewHolder.setOnSelectListener(new ValueIcInteractionListener());
                return textInputControlViewHolder;
            case IC_DATE_TIME:
                DateTimeView dateTimeItem = new DateTimeView(parent.getContext());
                dateTimeItem.setDateType(DateTimeView.DateType.DATE_TIME);
                DateTimeInputControlViewHolder dateTimeInputControlViewHolder = new DateTimeInputControlViewHolder(dateTimeItem);
                dateTimeInputControlViewHolder.setDateTimeClickListener(new DateIcInteractionListener());
                return dateTimeInputControlViewHolder;
            case IC_DATE:
                DateTimeView dateItem = new DateTimeView(parent.getContext());
                dateItem.setDateType(DateTimeView.DateType.DATE);
                DateTimeInputControlViewHolder dateInputControlViewHolder = new DateTimeInputControlViewHolder(dateItem);
                dateInputControlViewHolder.setDateTimeClickListener(new DateIcInteractionListener());
                return dateInputControlViewHolder;
            case IC_TIME:
                DateTimeView timeItem = new DateTimeView(parent.getContext());
                timeItem.setDateType(DateTimeView.DateType.TIME);
                DateTimeInputControlViewHolder timeInputControlViewHolder = new DateTimeInputControlViewHolder(timeItem);
                timeInputControlViewHolder.setDateTimeClickListener(new DateIcInteractionListener());
                return timeInputControlViewHolder;
            case IC_SINGLE_SELECT:
                listItem = layoutInflater.inflate(R.layout.item_input_control_value, parent, false);
                SingleSelectInputControlViewHolder singleSelectInputControlViewHolder = new SingleSelectInputControlViewHolder(listItem);
                singleSelectInputControlViewHolder.setOnSelectListener(new SingleSelectIcInteractionListener());
                return singleSelectInputControlViewHolder;
            case IC_MULTI_SELECT:
                listItem = layoutInflater.inflate(R.layout.item_input_control_value, parent, false);
                MultiSelectInputControlViewHolder multiSelectInputControlViewHolder = new MultiSelectInputControlViewHolder(listItem);
                multiSelectInputControlViewHolder.setOnSelectListener(new MultiSelectIcInteractionListener());
                return multiSelectInputControlViewHolder;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(BaseInputControlViewHolder viewHolder, int position) {
        mIsBinding = true;
        viewHolder.populateView(mInputControls.get(position));
        mIsBinding = false;
    }

    @Override
    public int getItemCount() {
        return mInputControls.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mInputControls.get(position).getType()) {
            case bool:
                return IC_BOOLEAN;
            case singleValueText:
                return IC_VALUE;
            case singleValueNumber:
                return IC_NUMBER_VALUE;
            case singleValueTime:
                return IC_TIME;
            case singleValueDate:
                return IC_DATE;
            case singleValueDatetime:
                return IC_DATE_TIME;
            case singleSelect:
            case singleSelectRadio:
                return IC_SINGLE_SELECT;
            case multiSelect:
            case multiSelectCheckbox:
                return IC_MULTI_SELECT;
            default:
                return IC_UNSUPPORTED;
        }
    }

    private void hideError(int position) {
        mInputControls.get(position).getState().setError(null);
    }

    public interface InputControlInteractionListener {
        void onBooleanStateChanged(InputControl inputControl, boolean newState);

        void onValueTextChanged(InputControl inputControl);

        void onSingleSelectIcClicked(InputControl inputControl);

        void onMultiSelectIcClicked(InputControl inputControl);

        void onDateIcClicked(InputControl inputControl);

        void onTimeIcClicked(InputControl inputControl);

        void onDateClear(InputControl inputControl);
    }

    private class BooleanIcInteractionListener implements BooleanInputControlViewHolder.StateChangeListener{
        @Override
        public void onStateChanged(int position, boolean state) {
            if (mIsBinding) return;

            if (mInteractionListener != null) {
                mInteractionListener.onBooleanStateChanged(mInputControls.get(position), state);
            }
        }
    }

    private class ValueIcInteractionListener implements ValueInputControlViewHolder.ClickListener{
        @Override
        public void onClick(int position) {
            if (mIsBinding) return;

            if (mInteractionListener != null) {
                mInteractionListener.onValueTextChanged(mInputControls.get(position));
            }
        }
    }

    private class DateIcInteractionListener implements DateTimeView.DateTimeClickListener {
        @Override
        public void onDateClick(int position) {
            if (mIsBinding) return;

            if (mInteractionListener != null) {
                mInteractionListener.onDateIcClicked(mInputControls.get(position));
            }
        }

        @Override
        public void onTimeClick(int position) {
            if (mIsBinding) return;

            if (mInteractionListener != null) {
                mInteractionListener.onTimeIcClicked(mInputControls.get(position));
            }
        }

        @Override
        public void onClear(int position) {
            if (mIsBinding) return;

            if (mInteractionListener != null) {
                mInteractionListener.onDateClear(mInputControls.get(position));
            }
        }
    }

    private class SingleSelectIcInteractionListener implements ValueInputControlViewHolder.ClickListener{
        @Override
        public void onClick(int position) {
            if (mIsBinding) return;

            if (mInteractionListener != null) {
                mInteractionListener.onSingleSelectIcClicked(mInputControls.get(position));
            }
        }
    }

    private class MultiSelectIcInteractionListener implements ValueInputControlViewHolder.ClickListener{
        @Override
        public void onClick(int position) {
            if (mIsBinding) return;

            if (mInteractionListener != null) {
                mInteractionListener.onMultiSelectIcClicked(mInputControls.get(position));
            }
        }
    }
}
