package com.cray.software.justreminder.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.datas.NoteItem;
import com.hexrain.design.fragments.NotePreviewFragment;

import java.util.ArrayList;

public class NotesAdapter extends BaseAdapter {

    LayoutInflater inflater;
    ArrayList<NoteItem> data;
    Context cContext;
    ColorSetter cs;
    SharedPrefs prefs;
    SyncHelper syncHelper;

    public NotesAdapter(Context context, ArrayList<NoteItem> data) {
        this.cContext = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        cs = new ColorSetter(cContext);
        prefs = new SharedPrefs(cContext);
        syncHelper = new SyncHelper(cContext);
    }

    public static class ViewHolder{
        ImageView image;
        TextView note;
        LinearLayout noteBackground;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            inflater = (LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_note, null);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.note = (TextView) convertView.findViewById(R.id.note);
            holder.noteBackground = (LinearLayout) convertView.findViewById(R.id.noteBackground);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String title = data.get(position).getNote();
        int color = data.get(position).getColor();
        int style = data.get(position).getStyle();
        byte[] byteImage = data.get(position).getImage();

        holder.note.setTypeface(cs.getTypeface(style));
        holder.noteBackground.setBackgroundColor(cs.getNoteLightColor(color));
        if (byteImage != null){
            Bitmap photo = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            if (photo != null){
                holder.image.setImageBitmap(photo);
            } else holder.image.setVisibility(View.GONE);
        } else holder.image.setVisibility(View.GONE);

        if (prefs.loadBoolean(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT)){
            title = syncHelper.decrypt(title);
        }
        holder.note.setText(title);
        holder.note.setTextSize(prefs.loadInt(Constants.APP_UI_PREFERENCES_TEXT_SIZE) + 12);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(data.get(position).getId(), holder.image);
            }
        });
        holder.note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(data.get(position).getId(), holder.image);
            }
        });
        holder.noteBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(data.get(position).getId(), holder.image);
            }
        });
        return convertView;
    }

    private void onItemClick(long id, ImageView imageView){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(cContext, NotePreviewFragment.class);
            intent.putExtra(Constants.EDIT_ID, id);
            String transitionName = "image";
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) cContext, imageView,
                            transitionName);
            cContext.startActivity(intent, options.toBundle());
        } else {
            cContext.startActivity(
                    new Intent(cContext, NotePreviewFragment.class)
                            .putExtra(Constants.EDIT_ID, id));
        }
    }
}