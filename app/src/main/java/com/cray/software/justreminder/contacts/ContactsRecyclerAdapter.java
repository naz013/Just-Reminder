package com.cray.software.justreminder.contacts;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databinding.ContactListItemBinding;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.ContactViewHolder> {

    private Context mContext;
    private List<ContactData> mDataList;
    private List<ContactData> mFiltered;

    private RecyclerClickListener mListener;

    public ContactsRecyclerAdapter(Context context, List<ContactData> dataItemList) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(dataItemList);
    }

    public ContactsRecyclerAdapter(Context context, List<ContactData> dataItemList, RecyclerClickListener listener) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(dataItemList);
        this.mListener = listener;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ContactViewHolder(DataBindingUtil.inflate(inflater, R.layout.contact_list_item, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        ContactData item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {

        ContactListItemBinding binding;

        public ContactViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.setClick(new AdapterListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public ContactData removeItem(int position) {
        final ContactData model = mDataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, ContactData model) {
        mDataList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final ContactData model = mDataList.remove(fromPosition);
        mDataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<ContactData> models) {
        this.mFiltered = models;
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<ContactData> newModels) {
        for (int i = mDataList.size() - 1; i >= 0; i--) {
            final ContactData model = mDataList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ContactData> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final ContactData model = newModels.get(i);
            if (!mDataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ContactData> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final ContactData model = newModels.get(toPosition);
            final int fromPosition = mDataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    @BindingAdapter("app:loadImage")
    public static void loadImage(ImageView imageView, String v) {
        boolean isDark = new ColorSetter(imageView.getContext()).isDark();
        Picasso.with(imageView.getContext())
                .load(Uri.parse(v))
                .resize(100, 100)
                .placeholder(isDark ? R.drawable.ic_person_outline_white_24dp : R.drawable.ic_person_outline_black_24dp)
                .error(isDark ? R.drawable.ic_person_outline_white_24dp : R.drawable.ic_person_outline_black_24dp)
                .transform(new CropCircleTransformation())
                .into(imageView);
    }
}
