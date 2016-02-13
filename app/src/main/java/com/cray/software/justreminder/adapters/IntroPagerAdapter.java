package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.models.IntroModel;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Copyright 2015 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class IntroPagerAdapter extends PagerAdapter {
    private List<IntroModel> fragments;
    private Context mContext;

    public IntroPagerAdapter(Context context, @NonNull List<IntroModel> fragments) {
        this.fragments = fragments;
        this.mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        IntroModel model = fragments.get(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_intro_layout, collection, false);
        TextView t = (TextView) layout.findViewById(R.id.title);
        ImageView i = (ImageView) layout.findViewById(R.id.image);
        LinearLayout m = (LinearLayout) layout.findViewById(R.id.main);
        t.setText(model.getTitle());
        m.setBackgroundColor(model.getColor());
        int drawable = model.getDrawable();
        Picasso.with(mContext)
                .load(drawable)
                .into(i);
        collection.addView(layout);
        return layout;
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getTitle();
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @NonNull
    public List<IntroModel> getFragments() {
        return fragments;
    }
}
