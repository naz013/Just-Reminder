package com.cray.software.justreminder.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.NotesManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.hexrain.design.fragments.NotePreviewFragment;

import java.util.ArrayList;

public class NotesRecyclerAdapter extends RecyclerView.Adapter<NotesRecyclerAdapter.ViewHolder> {

    private ArrayList<Note> data;
    private Context mContext;
    private ColorSetter cs;
    private SharedPrefs prefs;
    private SyncHelper syncHelper;

    public NotesRecyclerAdapter(Context context, ArrayList<Note> data) {
        this.mContext = context;
        this.data = data;
        cs = new ColorSetter(context);
        prefs = new SharedPrefs(context);
        syncHelper = new SyncHelper(context);
    }

    public interface EventListener {
        void onItemRemoved(int position);
    }

    @Override
    public NotesRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_note, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        String title = data.get(position).getNote();
        int color = data.get(position).getColor();
        int style = data.get(position).getStyle();
        byte[] byteImage = data.get(position).getImage();

        viewHolder.note.setTypeface(cs.getTypeface(style));
        viewHolder.noteBackground.setBackgroundColor(cs.getNoteLightColor(color));
        if (byteImage != null){
            Bitmap photo = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            if (photo != null){
                viewHolder.image.setImageBitmap(photo);
            } else viewHolder.image.setImageDrawable(null);
        } else viewHolder.image.setImageDrawable(null);

        if (prefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            title = syncHelper.decrypt(title);
        }
        viewHolder.note.setText(title);
        viewHolder.note.setTextSize(prefs.loadInt(Prefs.TEXT_SIZE) + 12);

        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(data.get(position).getId(), viewHolder.image);
            }
        });
        viewHolder.note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(data.get(position).getId(), viewHolder.image);
            }
        });
        viewHolder.noteBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(data.get(position).getId(), viewHolder.image);
            }
        });
    }

    private void onItemClick(long id, ImageView imageView){
        if (prefs.loadBoolean(Prefs.ITEM_PREVIEW)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(mContext, NotePreviewFragment.class);
                intent.putExtra(Constants.EDIT_ID, id);
                String transitionName = "image";
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, imageView,
                                transitionName);
                mContext.startActivity(intent, options.toBundle());
            } else {
                mContext.startActivity(
                        new Intent(mContext, NotePreviewFragment.class)
                                .putExtra(Constants.EDIT_ID, id));
            }
        } else {
            mContext.startActivity(new Intent(mContext, NotesManager.class)
                    .putExtra(Constants.EDIT_ID, id));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView note;
        LinearLayout noteBackground;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            image = (ImageView) itemLayoutView.findViewById(R.id.imageView);
            note = (TextView) itemLayoutView.findViewById(R.id.note);
            noteBackground = (LinearLayout) itemLayoutView.findViewById(R.id.noteBackground);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}