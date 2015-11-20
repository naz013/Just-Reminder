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
import com.cray.software.justreminder.datas.MarkerModel;
import com.cray.software.justreminder.datas.PlaceDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.AssetsUtil;

/**
 * Recycler view adapter for frequently used places.
 */
public class PlaceRecyclerAdapter extends RecyclerView.Adapter<PlaceRecyclerAdapter.ViewHolder> {

    /**
     * Application context field.
     */
    private Context mContext;

    /**
     * ColorSetter helper class field.
     */
    private ColorSetter cs;

    /**
     * Data provider for markers.
     */
    private PlaceDataProvider provider;

    /**
     * Font typeface for text view's.
     */
    private Typeface typeface;

    /**
     * Action listener for adapter.
     */
    private SimpleListener mEventListener;

    /**
     * Adapter constructor.
     * @param context application context.
     * @param provider places data provider.
     */
    public PlaceRecyclerAdapter(final Context context, final PlaceDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        cs = new ColorSetter(context);
        typeface = AssetsUtil.getLightTypeface(context);
        setHasStableIds(true);
    }

    /**
     * View holder for adapter.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnLongClickListener {

        /**
         * Place title.
         */
        public TextView textView;

        /**
         * Item card.
         */
        public CardView itemCard;

        /**
         * View holder constructor.
         * @param v view.
         */
        public ViewHolder(final View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.textView);
            textView.setTypeface(typeface);
            itemCard = (CardView) v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(5f);
            }

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            if (mEventListener != null) {
                mEventListener.onItemClicked(getAdapterPosition(), textView);
            }
        }

        @Override
        public boolean onLongClick(final View v) {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), textView);
            }
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_simple_card, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MarkerModel item = provider.getData().get(position);
        holder.textView.setText(item.getTitle());
    }

    @Override
    public int getItemViewType(final int position) {
        return 0;
    }

    @Override
    public long getItemId(final int position) {
        return provider.getData().get(position).getId();
    }

    @Override
    public int getItemCount() {
        return provider.getData().size();
    }

    /**
     * Get current action listener.
     * @return Action listener.
     */
    public SimpleListener getEventListener() {
        return mEventListener;
    }

    /**
     * Set action listener for adapter.
     * @param eventListener action listener.
     */
    public void setEventListener(final SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}
