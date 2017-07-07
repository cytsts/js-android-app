package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.filters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.MultiSelectActivity_;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.SingleSelectActivity;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.SingleSelectActivity_;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters.InputControlsAdapter;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.ItemSpaceDecoration;
import com.jaspersoft.android.jaspermobile.dialog.DateDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.TextInputControlDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.TextInputControlDialogFragment_;
import com.jaspersoft.android.jaspermobile.domain.ErrorSubscriber;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.adhoc.GetAdhocDataViewFiltersCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetInputControlsValuesCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.ValidateReportInputControlsCase;
import com.jaspersoft.android.jaspermobile.internal.di.components.FiltersFragmentComponent;
import com.jaspersoft.android.jaspermobile.util.IcDateHelper;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 * Created by aleksandrdakhno on 7/7/17.
 */

public class FiltersFragment extends Fragment
        implements InputControlsAdapter.InputControlInteractionListener,
        TextInputControlDialogFragment.InputControlValueDialogCallback,
        DateDialogFragment.IcDateDialogClickListener,
        SimpleDialogFragment.SimpleDialogClickListener {

    static final String ARG_FILTERS_URI = "filters_uri";

    public static final int SELECT_IC_REQUEST_CODE = 101;
    public static final String RESULT_SAME_PARAMS = "FiltersFragment.SAME_PARAMS";

    @Inject
    protected ReportParamsStorage paramsStorage;
    @Inject
    protected JasperServer mJasperServer;
    @Inject
    protected GetInputControlsValuesCase mGetInputControlsValuesCase;
    @Inject
    protected ValidateReportInputControlsCase mValidateReportInputControlsCase;
    @Inject
    GetAdhocDataViewFiltersCase mGetControlsCase;


    private RecyclerView mFiltersList;
    protected FloatingActionButton mApplyParamsBtn;
    private String mFiltersUri;
    private List<InputControl> mFilters;
    private InputControlsAdapter mAdapter;

    public static FiltersFragment newInstance(String filtersUri) {
        Bundle args = new Bundle();
        args.putString(ARG_FILTERS_URI, filtersUri);
        FiltersFragment fragment = new FiltersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFiltersUri = getArguments().getString(ARG_FILTERS_URI);
        assert mFiltersUri != null;

        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.fragment_filters, container, false);
        mFiltersList = (RecyclerView) v.findViewById(R.id.filtersList);
        mApplyParamsBtn = (FloatingActionButton) v.findViewById(R.id.btnApplyParams);
        mApplyParamsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenericSubscriber<List<InputControlState>> useCaseSubscriber =
                        new GenericSubscriber<>(new ValidateInputControlsValuesListener());
                mValidateReportInputControlsCase.execute(mFiltersUri, useCaseSubscriber);
            }
        });

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.f_s_label);
        }

        mFilters = paramsStorage.getInputControlHolder(mFiltersUri).getInputControls();
        if (mFilters == null) {
            mFilters = Collections.emptyList();
        }

        mGetControlsCase.execute(mFiltersUri, new GenericSubscriber<>(new SimpleSubscriber<Boolean>() {
            @Override
            public void onError(Throwable e) {
//                mFiltersVisible = false;
            }

            @Override
            public void onNext(Boolean hasControls) {
                if (hasControls) {
                    mFilters = paramsStorage.getInputControlHolder(mFiltersUri).getInputControls();
                    mAdapter.updateInputControlList(mFilters);
                }
//                mFiltersVisible = hasControls;
//                invalidateOptionsMenu();
            }
        }));

        showInputControls();

        return v;
    }

    @Override
    public void onStop() {
        mGetInputControlsValuesCase.unsubscribe();
        mValidateReportInputControlsCase.unsubscribe();
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == SELECT_IC_REQUEST_CODE) {
            if (data.hasExtra(SingleSelectActivity.SELECT_IC_ARG)) {
                String inputControlId = data.getStringExtra(SingleSelectActivity.SELECT_IC_ARG);
                InputControl selectInputControl = getInputControl(inputControlId);

                mAdapter.updateInputControl(selectInputControl);
                updateDependentControls(selectInputControl);
            }
        }
    }

    /*
     * Private methods
     */

    private void showInputControls() {
        mAdapter = new InputControlsAdapter(mFilters);
        mAdapter.setInteractionListener(this);
        int dividerHeight = (int) getResources().getDimension(R.dimen.ic_divider_height);
        int topPadding = (int) getResources().getDimension(R.dimen.ic_top_padding);

        mFiltersList.addItemDecoration(new ItemSpaceDecoration(dividerHeight, topPadding));
        mFiltersList.setItemAnimator(null);
        mFiltersList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFiltersList.setAdapter(mAdapter);
    }

    private FiltersFragmentComponent getComponent() {
        return GraphObject.Factory.from(getContext())
                .getProfileComponent()
                .plusFiltersPage();
    }

    /*
     * InputControlsAdapter.InputControlInteractionListener
     */

    @Override
    public void onBooleanStateChanged(InputControl inputControl, boolean newState) {
        inputControl.getState().setValue(String.valueOf(newState));
        mAdapter.updateInputControl(inputControl);
        updateDependentControls(inputControl);
    }

    @Override
    public void onValueTextChanged(InputControl inputControl) {
        TextInputControlDialogFragment_.createBuilder(getFragmentManager())
                .setInputControl(inputControl)
                .setTargetFragment(this)
                .show();
    }

    @Override
    public void onSingleSelectIcClicked(InputControl inputControl) {
        SingleSelectActivity_.intent(this)
                .reportUri(mFiltersUri)
                .inputControlId(inputControl.getId())
                .startForResult(SELECT_IC_REQUEST_CODE);
    }

    @Override
    public void onMultiSelectIcClicked(InputControl inputControl) {
        MultiSelectActivity_.intent(this)
                .reportUri(mFiltersUri)
                .inputControlId(inputControl.getId())
                .startForResult(SELECT_IC_REQUEST_CODE);
    }

    @Override
    public void onDateIcClicked(InputControl inputControl) {
        DateDialogFragment.createBuilder(getFragmentManager())
                .setInputControlId(inputControl.getId())
                .setDate(IcDateHelper.convertToDate(inputControl))
                .setType(DateDialogFragment.DATE)
                .setTargetFragment(this)
                .show();
    }

    @Override
    public void onTimeIcClicked(InputControl inputControl) {
        DateDialogFragment.createBuilder(getFragmentManager())
                .setInputControlId(inputControl.getId())
                .setDate(IcDateHelper.convertToDate(inputControl))
                .setType(DateDialogFragment.TIME)
                .setTargetFragment(this)
                .show();
    }

    @Override
    public void onDateClear(InputControl inputControl) {
        inputControl.getState().setValue("");
        mAdapter.updateInputControl(inputControl);
        updateDependentControls(inputControl);
    }

    /*
     * TextInputControlDialogFragment.InputControlValueDialogCallback
     */

    @Override
    public void onTextValueEntered(InputControl inputControl, String text) {
        inputControl.getState().setValue(text);
        mAdapter.updateInputControl(inputControl);
        updateDependentControls(inputControl);
    }

    /*
     * DateDialogFragment.IcDateDialogClickListener
     */

    @Override
    public void onDateSelected(Calendar date, int requestCode, Object... data) {
        String icId = (String) data[0];
        InputControl inputControl = getInputControl(icId);

        updateDateValue(inputControl, date);
        mAdapter.updateInputControl(inputControl);
        updateDependentControls(inputControl);
    }

    /*
     * SimpleDialogFragment.SimpleDialogClickListener
     */

    @Override
    public void onPositiveClick(int requestCode) {

    }

    @Override
    public void onNegativeClick(int requestCode) {

    }

    /*
     *
     */

    private final class GenericSubscriber<T> extends ErrorSubscriber<T> {
        private GenericSubscriber(SimpleSubscriber<T> delegate) {
            super(delegate);
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "AdHoc Data View thrown error");
            super.onError(e);
        }
    }

    private class GetInputControlsValuesListener extends SimpleSubscriber<List<InputControlState>> {
        @Override
        public void onNext(List<InputControlState> stateList) {
            updateInputControls(stateList);
        }
    }

    private class ValidateInputControlsValuesListener extends SimpleSubscriber<List<InputControlState>> {
        @Override
        public void onNext(List<InputControlState> stateList) {
            if (stateList.isEmpty()) {
                onValidationPassed();
            } else {
                updateInputControls(stateList);
            }
        }

        void onValidationPassed() {
            applyFiltersAction();
        }
    }

    /*
     *
     */

    private InputControl getInputControl(String id) {
        for (InputControl inputControl : mFilters) {
            if (inputControl.getId().equals(id)) {
                return inputControl;
            }
        }
        return null;
    }

    private void updateDependentControls(InputControl inputControl) {
        if (!inputControl.getSlaveDependencies().isEmpty()) {
            mGetInputControlsValuesCase.execute(mFiltersUri, new GenericSubscriber<>(new GetInputControlsValuesListener()));
        }
    }

    private void updateInputControls(List<InputControlState> stateList) {
        for (InputControlState inputControlState : stateList) {
            InputControl inputControl = getInputControl(inputControlState.getId());
            if (inputControl != null) {
                if (inputControl.getType() == InputControl.Type.bool && inputControlState.getValue().equals(InputControlWrapper.NULL_SUBSTITUTE)) {
                    inputControlState.setValue("false");
                }
                inputControl.setState(inputControlState);
            }
        }
        mAdapter.updateInputControlList(mFilters);
    }

    private void applyFiltersAction() {
        Intent i = new Intent();
        ArrayList<ReportParameter> parameters = initParametersUsingSelectedValues();
        if (isNewParamsEqualOld(parameters)) {
            i.putExtra(RESULT_SAME_PARAMS, true);
        } else {
            i.putExtra(RESULT_SAME_PARAMS, false);
        }
        paramsStorage.getInputControlHolder(mFiltersUri).setReportParams(parameters);
        getActivity().setResult(Activity.RESULT_OK, i);
        getActivity().finish();
    }

    private ArrayList<ReportParameter> initParametersUsingSelectedValues() {
        ArrayList<ReportParameter> parameters = new ArrayList<>();
        for (InputControl inputControl : mFilters) {
            parameters.add(new ReportParameter(inputControl.getId(), inputControl.getSelectedValues()));
        }
        return parameters;
    }

    private boolean isNewParamsEqualOld(ArrayList<ReportParameter> newParams) {
        List<ReportParameter> oldParams = paramsStorage.getInputControlHolder(mFiltersUri).getReportParams();

        if (oldParams.size() != newParams.size()) {
            return false;
        }

        for (int i = 0; i < oldParams.size(); i++) {
            if (!oldParams.get(i).getValues().equals(newParams.get(i).getValues())) return false;
        }

        return true;
    }

    private void updateDateValue(InputControl inputControl, Calendar newDate) {
        String newDateString = IcDateHelper.convertToString(inputControl, newDate);
        inputControl.getState().setValue(newDateString);
    }
}
