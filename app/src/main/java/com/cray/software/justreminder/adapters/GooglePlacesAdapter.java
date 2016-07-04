/*
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.models.PlaceModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.ArrayList;

/**
 * Recycler view adapter for frequently used places.
 */
public class GooglePlacesAdapter extends RecyclerView.Adapter<GooglePlacesAdapter.ViewHolder> {

    private ArrayList<PlaceModel> array = new ArrayList<>();

    /**
     * Action listener for adapter.
     */
    private SimpleListener mEventListener;
    private boolean isDark;

    /**
     * Adapter constructor.
     * @param context application context.
     * @param array places data provider.
     */
    public GooglePlacesAdapter(final Context context, ArrayList<PlaceModel> array) {
        this.array = array;
        isDark = new ColorSetter(context).isDark();
        setHasStableIds(true);
    }

    /**
     * View holder for adapter.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        /**
         * Place title.
         */
        public RoboTextView textView, text2;
        public RelativeLayout listItem;
        public ImageView placeIcon;
        public RoboCheckBox placeCheck;

        /**
         * View holder constructor.
         * @param v view.
         */
        public ViewHolder(final View v) {
            super(v);
            listItem = (RelativeLayout) v.findViewById(R.id.listItem);
            placeIcon = (ImageView) v.findViewById(R.id.placeIcon);
            placeCheck = (RoboCheckBox) v.findViewById(R.id.placeCheck);
            textView = (RoboTextView) v.findViewById(R.id.text1);
            text2 = (RoboTextView) v.findViewById(R.id.text2);

            v.setOnClickListener(this);
            placeCheck.setOnClickListener(v1 -> {
                PlaceModel item = array.get(getAdapterPosition());
                if (item.getSelected() == 0) item.setSelected(1);
                else item.setSelected(0);
                notifyDataSetChanged();
            });
        }

        @Override
        public void onClick(final View v) {
            if (getAdapterPosition() == getLast() && getItemCount() > 1) {
                for (PlaceModel item : array) item.setSelected(1);
                notifyDataSetChanged();
            } else {
                if (mEventListener != null) {
                    mEventListener.onItemClicked(getAdapterPosition(), listItem);
                }
            }
        }
    }

    private int getLast() {
        return getItemCount() - 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_text_item_advanced, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        PlaceModel item = array.get(position);
        holder.textView.setText(item.getName());
        holder.text2.setText(item.getAddress());
        int icon = getIcon(item.getTypes());
        if (icon != 0) holder.placeIcon.setImageResource(icon);
        if (item.getSelected() == 1) holder.placeCheck.setChecked(true);
        else holder.placeCheck.setChecked(false);

        if (position == getLast() && getItemCount() > 1) {
            holder.placeCheck.setVisibility(View.GONE);
            holder.placeIcon.setVisibility(View.GONE);
            holder.text2.setText("");
        }
    }

    @Override
    public int getItemViewType(final int position) {
        return 0;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    private int getIcon(ArrayList<String> tags) {
        if (tags == null) {
            if (isDark) return R.drawable.ic_place_white_vector;
            else return R.drawable.ic_place_black_vector;
        }

        StringBuilder sb = new StringBuilder();
        for (String t : tags) sb.append(t).append(",");
        String tag = sb.toString();
        if (tag.contains("florist")) {
            if (isDark) return R.drawable.ic_local_florist_white_24dp;
            else return R.drawable.ic_local_florist_black_24dp;
        } else if (tag.contains("cafe")) {
            if (isDark) return R.drawable.ic_local_cafe_white_24dp;
            else return R.drawable.ic_local_cafe_black_24dp;
        } else if (tag.contains("restaurant")) {
            if (isDark) return R.drawable.ic_restaurant_white_24dp;
            else return R.drawable.ic_restaurant_black_24dp;
        } else if (tag.contains("bus_station")) {
            if (isDark) return R.drawable.ic_directions_bus_white_24dp;
            else return R.drawable.ic_directions_bus_black_24dp;
        } else if (tag.contains("subway_station")) {
            if (isDark) return R.drawable.ic_subway_white_24dp;
            else return R.drawable.ic_subway_black_24dp;
        } else if (tag.contains("train_station")) {
            if (isDark) return R.drawable.ic_train_white_24dp;
            else return R.drawable.ic_train_black_24dp;
        } else if (tag.contains("bicycle_store")) {
            if (isDark) return R.drawable.ic_directions_bike_white_24dp;
            else return R.drawable.ic_directions_bike_black_24dp;
        } else if (tag.contains("car_repair") || tag.contains("car_rental") || tag.contains("car_dealer")) {
            if (isDark) return R.drawable.ic_directions_car_white_24dp;
            else return R.drawable.ic_directions_car_black_24dp;
        } else if (tag.contains("taxi") || tag.contains("taxi_stand")) {
            if (isDark) return R.drawable.ic_local_taxi_white_24dp;
            else return R.drawable.ic_local_taxi_black_24dp;
        } else if (tag.contains("atm")) {
            if (isDark) return R.drawable.ic_local_atm_white_24dp;
            else return R.drawable.ic_local_atm_black_24dp;
        } else if (tag.contains("bar")) {
            if (isDark) return R.drawable.ic_local_bar_white_24dp;
            else return R.drawable.ic_local_bar_black_24dp;
        } else if (tag.contains("airport")) {
            if (isDark) return R.drawable.ic_local_airport_white_24dp;
            else return R.drawable.ic_local_airport_black_24dp;
        } else if (tag.contains("car_wash")) {
            if (isDark) return R.drawable.ic_local_car_wash_white_24dp;
            else return R.drawable.ic_local_car_wash_black_24dp;
        } else if (tag.contains("convenience_store")) {
            if (isDark) return R.drawable.ic_local_convenience_store_white_24dp;
            else return R.drawable.ic_local_convenience_store_black_24dp;
        } else if (tag.contains("gas_station")) {
            if (isDark) return R.drawable.ic_local_gas_station_white_24dp;
            else return R.drawable.ic_local_gas_station_black_24dp;
        } else if (tag.contains("hospital") || tag.contains("doctor") ||
                tag.contains("physiotherapist") || tag.contains("health")) {
            if (isDark) return R.drawable.ic_local_hospital_white_24dp;
            else return R.drawable.ic_local_hospital_black_24dp;
        } else if (tag.contains("grocery_or_supermarket")) {
            if (isDark) return R.drawable.ic_local_grocery_store_white_24dp;
            else return R.drawable.ic_local_grocery_store_black_24dp;
        } else if (tag.contains("night_club") || tag.contains("liquor_store")) {
            if (isDark) return R.drawable.ic_local_drink_white_24dp;
            else return R.drawable.ic_local_drink_black_24dp;
        } else if (tag.contains("meal_takeaway")) {
            if (isDark) return R.drawable.ic_local_pizza_white_24dp;
            else return R.drawable.ic_local_pizza_black_24dp;
        } else if (tag.contains("pharmacy")) {
            if (isDark) return R.drawable.ic_local_pharmacy_white_24dp;
            else return R.drawable.ic_local_pharmacy_black_24dp;
        } else if (tag.contains("meal_delivery") || tag.contains("moving_company")) {
            if (isDark) return R.drawable.ic_local_shipping_white_24dp;
            else return R.drawable.ic_local_shipping_black_24dp;
        } else if (tag.contains("parking")) {
            if (isDark) return R.drawable.ic_local_parking_white_24dp;
            else return R.drawable.ic_local_parking_black_24dp;
        } else if (tag.contains("electronics_store")) {
            if (isDark) return R.drawable.ic_local_printshop_white_24dp;
            else return R.drawable.ic_local_printshop_black_24dp;
        } else if (tag.contains("laundry")) {
            if (isDark) return R.drawable.ic_local_laundry_service_white_24dp;
            else return R.drawable.ic_local_laundry_service_black_24dp;
        } else if (tag.contains("book_store") || tag.contains("library")) {
            if (isDark) return R.drawable.ic_local_library_white_24dp;
            else return R.drawable.ic_local_library_black_24dp;
        } else if (tag.contains("post_office")) {
            if (isDark) return R.drawable.ic_local_post_office_white_24dp;
            else return R.drawable.ic_local_post_office_black_24dp;
        } else if (tag.contains("movie_rental") || tag.contains("movie_theater")) {
            if (isDark) return R.drawable.ic_local_movies_white_24dp;
            else return R.drawable.ic_local_movies_black_24dp;
        } else if (tag.contains("real_estate_agency") || tag.contains("establishment")) {
            if (isDark) return R.drawable.ic_local_hotel_white_24dp;
            else return R.drawable.ic_local_hotel_black_24dp;
        } else if (tag.contains("clothing_store") || tag.contains("home_goods_store")
                || tag.contains("shopping_mall") || tag.contains("shoe_store")) {
            if (isDark) return R.drawable.ic_local_mall_white_24dp;
            else return R.drawable.ic_local_mall_black_24dp;
        } else if (tag.contains("food")) {
            if (isDark) return R.drawable.ic_local_dining_white_24dp;
            else return R.drawable.ic_local_dining_black_24dp;
        } else {
            if (isDark) return R.drawable.ic_place_white_vector;
            else return R.drawable.ic_place_black_vector;
        }
    }

    /**
     * Set action listener for adapter.
     * @param eventListener action listener.
     */
    public void setEventListener(final SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}
