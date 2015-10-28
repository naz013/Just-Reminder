package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.NoteModel;
import com.cray.software.justreminder.datas.NoteDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.SimpleListener;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private ColorSetter cs;
    private NoteDataProvider provider;
    private SharedPrefs prefs;
    private SyncHelper syncHelper;
    private SimpleListener mEventListener;

    public NoteRecyclerAdapter(Context context, NoteDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        prefs = new SharedPrefs(context);
        syncHelper = new SyncHelper(context);
        cs = new ColorSetter(context);
        setHasStableIds(true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewGroup container;
        LinearLayout noteBackground;
        ImageView noteImage;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.note);
            container = (ViewGroup) v.findViewById(R.id.container);
            noteBackground = (LinearLayout) v.findViewById(R.id.noteBackground);
            noteImage = (ImageView) v.findViewById(R.id.noteImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_note, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final NoteModel item = provider.getData().get(position);
        String title = item.getNote();
        int color = item.getColor();
        int style = item.getStyle();
        byte[] byteImage = item.getImage();

        holder.textView.setTypeface(cs.getTypeface(style));
        holder.noteBackground.setBackgroundColor(cs.getNoteLightColor(color));
        if (byteImage != null){
            Bitmap photo = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            if (photo != null){
                holder.noteImage.setImageBitmap(photo);
            } else holder.noteImage.setImageDrawable(null);
        } else holder.noteImage.setImageDrawable(null);

        if (prefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            title = syncHelper.decrypt(title);
        }
        holder.textView.setText(title);
        holder.textView.setTextSize(prefs.loadInt(Prefs.TEXT_SIZE) + 12);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEventListener != null) mEventListener.onItemClicked(position, holder.noteImage);
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mEventListener != null) mEventListener.onItemLongClicked(position, holder.noteImage);
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