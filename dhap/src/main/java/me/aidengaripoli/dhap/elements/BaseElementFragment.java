package me.aidengaripoli.dhap.elements;

import android.content.Context;

import androidx.fragment.app.Fragment;

public abstract class BaseElementFragment extends Fragment {

    private OnElementCommandListener listener;

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
}
