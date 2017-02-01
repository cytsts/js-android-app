package com.jaspersoft.android.jaspermobile.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
public class ReportErrorActionView extends FrameLayout {
    public static final int NO_ACTION = 0;
    public static final int APPLY_FILTERS_ACTION = 1;
    public static final int RELOAD_ACTION = 2;

    @BindView(R.id.reportMessageText)
    TextView reportErrorMessage;
    @BindView(R.id.reportHandleAction)
    Button reportHandleAction;

    private int action;

    public ReportErrorActionView(Context context) {
        super(context);
        init();
    }

    public ReportErrorActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReportErrorActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ReportErrorActionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_report_message, this);
        ButterKnife.bind(this);

        action = NO_ACTION;
    }

    public void setOnActionClickListener(OnClickListener onClickListener) {
        reportHandleAction.setOnClickListener(onClickListener);
    }

    public void showError(String error, int handleAction) {
        action = handleAction;

        reportErrorMessage.setText(error);
        updateHandleActionButton();
    }

    public int getAction() {
        return action;
    }

    private void updateHandleActionButton() {
        reportHandleAction.setVisibility(action == NO_ACTION ? GONE : VISIBLE);

        if (action == APPLY_FILTERS_ACTION) {
            reportHandleAction.setText(R.string.rv_apply_filters_action);
        } else  if (action == RELOAD_ACTION) {
            reportHandleAction.setText(R.string.rv_dialog_reload);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.isVisible = getVisibility() == VISIBLE;
        ss.message = reportErrorMessage.getText().toString();
        ss.action = action;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        action = ss.action;
        setVisibility(ss.isVisible ? VISIBLE : GONE);
        showError(ss.message, ss.action);
    }

    static class SavedState extends BaseSavedState {
        boolean isVisible;
        String message;
        int action;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel source) {
            super(source);
            this.isVisible = source.readInt() == 1;
            this.message = source.readString();
            this.action = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(isVisible ? 1 : 0);
            out.writeString(message);
            out.writeInt(action);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
