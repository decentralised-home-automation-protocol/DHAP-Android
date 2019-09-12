package me.aidengaripoli.dhap.display.elements;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public abstract class BaseElementFragment extends Fragment {

    static final String ARG_LABEL = "label";
    static final int ARG_LABEL_INDEX = 0;
    private static final String NO_LABEL = "~";

    TextView labelView;
    String label;
    private String Id;
    private OnElementCommandListener listener;

    public abstract void updateFragmentData(String value);

    void addLabel() {
        if (label == null) return;

        if (label.equals(NO_LABEL)) {
            labelView.setVisibility(View.GONE);
        } else {
            labelView.setText(label);
        }
    }

    public void sendMessage(String data) {
        if (listener != null) {
            listener.onElementCommand(Id, data);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnElementCommandListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setId(String Id) {
        this.Id = Id;
    }
}
