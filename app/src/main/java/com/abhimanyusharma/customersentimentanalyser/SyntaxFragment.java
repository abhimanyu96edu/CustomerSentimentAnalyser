package com.abhimanyusharma.customersentimentanalyser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abhimanyusharma.customersentimentanalyser.model.TokenInfo;
import com.google.android.flexbox.FlexboxLayout;


public class SyntaxFragment extends Fragment {

    private static final String ARG_TOKENS = "tokens";
    private FlexboxLayout mLayout;

    public static SyntaxFragment newInstance() {
        final SyntaxFragment fragment = new SyntaxFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_syntax, container, false);
        mLayout = (FlexboxLayout) view.findViewById(R.id.layout);
        TokenInfo[] tokens = (TokenInfo[]) getArguments().getParcelableArray(ARG_TOKENS);
        if (tokens != null) {
            showTokens(tokens);
        }
        return view;
    }

    private void showTokens(TokenInfo[] tokens) {
        mLayout.removeAllViews();
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        for (TokenInfo token : tokens) {
            final View view = inflater.inflate(R.layout.item_token, mLayout, false);
            TextView text = (TextView) view.findViewById(R.id.text);
            TextView label = (TextView) view.findViewById(R.id.label);
            TextView partOfSpeech = (TextView) view.findViewById(R.id.part_of_speech);
            text.setText(token.text);
            label.setText(token.label != null ? token.label.toLowerCase() : null);
            partOfSpeech.setText(token.partOfSpeech);
            mLayout.addView(view);
        }
    }

    public void setTokens(TokenInfo[] tokens) {

        showTokens(tokens);
        getArguments().putParcelableArray(ARG_TOKENS, tokens);
    }
}
