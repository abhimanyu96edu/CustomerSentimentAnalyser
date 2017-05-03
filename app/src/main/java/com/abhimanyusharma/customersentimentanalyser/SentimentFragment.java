package com.abhimanyusharma.customersentimentanalyser;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhimanyusharma.customersentimentanalyser.model.SentimentInfo;


public class SentimentFragment extends Fragment {

    private static final String ARG_SENTIMENT = "sentiment";

    public static SentimentFragment newInstance() {
        final SentimentFragment fragment = new SentimentFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private int mColorPositive;
    private int mColorNeutral;
    private int mColorNegative;

    private TextView mPolarity;
    private TextView mMagnitude;
    private ImageView mEmo;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources resources = getResources();
        final Resources.Theme theme = getActivity().getTheme();
        mColorPositive = ResourcesCompat.getColor(resources, R.color.polarity_positive, theme);
        mColorNeutral = ResourcesCompat.getColor(resources, R.color.polarity_neutral, theme);
        mColorNegative = ResourcesCompat.getColor(resources, R.color.polarity_negative, theme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sentiment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mPolarity = (TextView) view.findViewById(R.id.polarity);
        mMagnitude = (TextView) view.findViewById(R.id.magnitude);
        mEmo=(ImageView) view.findViewById(R.id.emo);
        final Bundle args = getArguments();
        if (args.containsKey(ARG_SENTIMENT)) {
            showSentiment((SentimentInfo) args.getParcelable(ARG_SENTIMENT));
        }
    }

    private void showSentiment(SentimentInfo sentiment) {
        mPolarity.setText(String.valueOf(sentiment.polarity));
        if (sentiment.polarity > 0.45) {
            mPolarity.setBackgroundColor(mColorPositive);
            mEmo.setImageResource(R.drawable.emo_happy);
        } else if (sentiment.polarity > -0.75) {
            mPolarity.setBackgroundColor(mColorNeutral);
            mEmo.setImageResource(R.drawable.emo_neu);
        } else {
            mPolarity.setBackgroundColor(mColorNegative);
            mEmo.setImageResource(R.drawable.emo_ang);
        }
        mMagnitude.setText(String.valueOf(sentiment.magnitude));
    }

    public void setSentiment(SentimentInfo sentiment) {

        showSentiment(sentiment);
        getArguments().putParcelable(ARG_SENTIMENT, sentiment);
        // mEmo.setImageResource(R.drawable.emo_happy);
    }
}
