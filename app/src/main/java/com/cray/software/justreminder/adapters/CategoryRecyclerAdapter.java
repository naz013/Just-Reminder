package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.datas.CategoryDataProvider;
import com.cray.software.justreminder.datas.models.CategoryModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.AssetsUtil;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private ColorSetter cs;
    private CategoryDataProvider provider;
    private Typeface typeface;
    private SimpleListener mEventListener;

    public CategoryRecyclerAdapter(Context context, CategoryDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        cs = new ColorSetter(context);
        typeface = AssetsUtil.getLightTypeface(context);
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {

        public TextView textView;
        public CardView itemCard;
        public View indicator;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.textView);
            indicator = v.findViewById(R.id.indicator);
            itemCard = (CardView) v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mEventListener != null) {
                mEventListener.onItemClicked(getAdapterPosition(), indicator);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), indicator);
            }
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_category_card, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final CategoryModel item = provider.getData().get(position);
        String title = item.getTitle();
        int indicator = item.getColor();

        holder.textView.setTypeface(typeface);
        holder.textView.setText(title);
        holder.indicator.setBackgroundResource(cs.getCategoryIndicator(indicator));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return provider.getData().get(position).getId();
    }

    @Override
    public int getItemCount() {
        return provider.getData().size();
    }

    public SimpleListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}