package me.aidengaripoli.dhapexample;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ActionFragment extends Fragment {

    private static final String ARG_ACTION_TEXT = "actionText";
    private static final String ARG_ACTION_ENABLED = "actionEnabled";
    private static final String ARG_SECONDARY_ACTION_TEXT = "secondaryActionText";

    private String mActionText;
    private String mSecondaryActionText;
    private boolean mActionEnabled;

    private OnActionResultListener mListener;

    public ActionFragment() {
    }

    static ActionFragment newInstance(String primaryActionText, boolean primaryActionEnabled,
                                      String secondaryActionText) {
        ActionFragment fragment = new ActionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACTION_TEXT, primaryActionText);
        args.putBoolean(ARG_ACTION_ENABLED, primaryActionEnabled);
        args.putString(ARG_SECONDARY_ACTION_TEXT, secondaryActionText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mActionText = getArguments().getString(ARG_ACTION_TEXT);
            mActionEnabled = getArguments().getBoolean(ARG_ACTION_ENABLED);
            mSecondaryActionText = getArguments().getString(ARG_SECONDARY_ACTION_TEXT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_action, container, false);

        Button actionButton = rootView.findViewById(R.id.button_action);
        actionButton.setText(mActionText);
        actionButton.setEnabled(mActionEnabled);
        actionButton.setOnClickListener(v -> onActionButtonPressed("primary"));

        Button secondaryActionButton = rootView.findViewById(R.id.button_secondary_action);
        if (!mSecondaryActionText.isEmpty()) {
            secondaryActionButton.setText(mSecondaryActionText);
            secondaryActionButton.setOnClickListener(v -> onActionButtonPressed("secondary"));
        } else {
            secondaryActionButton.setVisibility(View.INVISIBLE);
        }

        Button clearButton = rootView.findViewById(R.id.button_action2);
        clearButton.setOnClickListener(v -> mListener.onActionResult("Clear"));

        Button DiscoverButton = rootView.findViewById(R.id.button_discover);
        DiscoverButton.setOnClickListener(v -> mListener.onActionResult("Discovery"));
        return rootView;
    }

    private void onActionButtonPressed(String actionButton) {
        if (mListener != null) {
            if (actionButton.equals("primary")) {
                mListener.onActionResult(mActionText);
            } else {
                mListener.onActionResult(mSecondaryActionText);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnActionResultListener) {
            mListener = (OnActionResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnActionResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnActionResultListener {
        void onActionResult(String action);
    }

}
