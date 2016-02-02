package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.ArrayList;

public class BirthdaysVoiceList extends Activity{

    private ListView contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(BirthdaysVoiceList.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.birthdays_list_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Intent intent = getIntent();
        ArrayList<String> mNames = intent.getStringArrayListExtra("names");
        ArrayList<String> mDates = intent.getStringArrayListExtra("dates");
        ArrayList<Integer> mYears = intent.getIntegerArrayListExtra("years");

        contactsList = (ListView) findViewById(R.id.contactsList);

        TextView aboutClose = (TextView) findViewById(R.id.aboutClose);
        aboutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loaderAdapter(mNames, mDates, mYears);
    }

    public void loaderAdapter(ArrayList<String> names, ArrayList<String> dates, ArrayList<Integer> years){
        CustomArray customAdapter = new CustomArray(BirthdaysVoiceList.this, names, dates, years);
        contactsList.setAdapter(customAdapter);
    }

    public class CustomArray extends BaseAdapter{

        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();
        ArrayList<Integer> years = new ArrayList<>();
        LayoutInflater inflater;
        Context mContext;

        public CustomArray(Context context, ArrayList<String> names, ArrayList<String> dates, ArrayList<Integer> years){
            this.mContext = context;
            this.names = names;
            this.dates = dates;
            this.years = years;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return names.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_featured_events, null);
            }

            TextView userText = (TextView) convertView.findViewById(R.id.userText);
            TextView dateText = (TextView) convertView.findViewById(R.id.dateText);
            TextView yearsText = (TextView) convertView.findViewById(R.id.yearsText);

            userText.setText(names.get(position));
            dateText.setText(dates.get(position));
            yearsText.setText(TimeUtil.getAgeFormatted(mContext, years.get(position)));

            return convertView;
        }
    }
}
