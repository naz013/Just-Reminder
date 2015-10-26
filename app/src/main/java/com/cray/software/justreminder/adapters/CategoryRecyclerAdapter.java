package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.Category;
import com.cray.software.justreminder.datas.CategoryDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.ViewUtils;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private ColorSetter cs;
    private CategoryDataProvider provider;
    private Typeface typeface;
    private SimpleListener mEventListener;

    public CategoryRecyclerAdapter(Context context, CategoryDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        SharedPrefs prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        typeface = AssetsUtil.getLightTypeface(context);
        setHasStableIds(true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ViewGroup container;
        RelativeLayout background;
        View indicator;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.textView);
            container = (ViewGroup) v.findViewById(R.id.container);
            indicator = v.findViewById(R.id.indicator);
            background = (RelativeLayout) v.findViewById(R.id.background);
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
        holder.background.setBackgroundResource(cs.getCardDrawableStyle());

        final Category item = provider.getData().get(position);
        String title = item.getTitle();
        int indicator = item.getColor();

        holder.textView.setTypeface(typeface);
        holder.textView.setText(title);
        holder.indicator.setBackgroundDrawable(ViewUtils.getDrawable(mContext, cs.getCategoryIndicator(indicator)));

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEventListener != null) mEventListener.onItemClicked(position, holder.indicator);
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mEventListener != null)
                    mEventListener.onItemLongClicked(position, holder.indicator);
                return true;
            }
        });
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