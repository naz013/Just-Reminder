package com.cray.software.justreminder.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cray.software.justreminder.R;

public class StartFragment extends Fragment {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";

    int pageNumber;
    Typeface typeface;

    public static StartFragment newInstance(int page) {
        StartFragment pageFragment = new StartFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.guide_fragment, null);

        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        TextView titleHelp = (TextView) view.findViewById(R.id.fragmentTitle);
        titleHelp.setTypeface(typeface);
        ImageView helpIcon = (ImageView) view.findViewById(R.id.fragmentImage);
        if (pageNumber == 0){
            helpIcon.setImageResource(R.drawable.flexible_tr);
            titleHelp.setText(getActivity().getString(R.string.guide_title_flexible));
        } else if (pageNumber == 1){
            helpIcon.setImageResource(R.drawable.smart_tr);
            titleHelp.setText(getActivity().getString(R.string.guide_smart_title));
        } else if (pageNumber == 2){
            helpIcon.setImageResource(R.drawable.dark_tr);
            titleHelp.setText(getActivity().getString(R.string.dark_theme_guide_title));
        } else if (pageNumber == 3){
            helpIcon.setImageResource(R.drawable.widgets_tr);
            titleHelp.setText(getActivity().getString(R.string.widgets_support_guide_title));
        } else if (pageNumber == 4){
            helpIcon.setImageResource(R.drawable.cloud_tr);
            titleHelp.setText(getActivity().getString(R.string.guide_sync_title));
        }

        return view;
    }
}