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

package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.CategoryManager;
import com.cray.software.justreminder.NotesManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.activities.AddBirthday;
import com.cray.software.justreminder.adapters.FileRecyclerAdapter;
import com.cray.software.justreminder.async.DeleteAsync;
import com.cray.software.justreminder.async.UserInfoAsync;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.datas.FileDataProvider;
import com.cray.software.justreminder.datas.models.FileModel;
import com.cray.software.justreminder.datas.models.UserModel;
import com.cray.software.justreminder.graph.PieGraph;
import com.cray.software.justreminder.graph.PieSlice;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.interfaces.DataListener;
import com.cray.software.justreminder.interfaces.LCAMListener;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.MemoryUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.PaperButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DropboxFragment extends Fragment implements SimpleListener, SyncListener, DataListener {

    private static final String TYPE = "window_type";
    private static final String FILE_NAME = "Google_photo.jpg";

    private TextView cloudUser;
    private TextView cloudCount;
    private TextView usedSpace;
    private TextView freeSpace;
    private RecyclerView filesCloudList;
    private PieGraph usedSizeGraph;
    private LinearLayout cloudContainer;
    private ImageView userPhoto;

    private FileDataProvider provider;

    private NavigationCallbacks mCallbacks;
    private boolean isDeleted;

    private int type;

    public static DropboxFragment newInstance(int type) {
        DropboxFragment fragment = new DropboxFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public DropboxFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(TYPE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cloud_layout, container, false);

        Typeface typefaceMedium = AssetsUtil.getMediumTypeface(getActivity());
        Typeface typefaceThin = AssetsUtil.getThinTypeface(getActivity());

        cloudContainer = (LinearLayout) view.findViewById(R.id.cloudContainer);
        cloudContainer.setVisibility(View.VISIBLE);

        CardView card1 = (CardView) view.findViewById(R.id.card1);
        CardView card2 = (CardView) view.findViewById(R.id.card2);
        CardView card3 = (CardView) view.findViewById(R.id.card3);
        ColorSetter colorSetter = new ColorSetter(getActivity());
        card1.setCardBackgroundColor(colorSetter.getCardStyle());
        card2.setCardBackgroundColor(colorSetter.getCardStyle());
        card3.setCardBackgroundColor(colorSetter.getCardStyle());
        if (Module.isLollipop()) {
            card3.setCardElevation(Configs.CARD_ELEVATION_REMINDER);
            card2.setCardElevation(Configs.CARD_ELEVATION_REMINDER);
            card1.setCardElevation(Configs.CARD_ELEVATION_REMINDER);
        }

        cloudUser = (TextView) view.findViewById(R.id.cloudUser);
        cloudUser.setTypeface(typefaceThin);

        userPhoto = (ImageView) view.findViewById(R.id.userPhoto);
        userPhoto.setVisibility(View.INVISIBLE);

        usedSizeGraph = (PieGraph) view.findViewById(R.id.usedSizeGraph);

        TextView cloudText = (TextView) view.findViewById(R.id.cloudText);
        cloudText.setTypeface(typefaceThin);

        cloudCount = (TextView) view.findViewById(R.id.cloudCount);
        cloudCount.setTypeface(typefaceMedium);

        usedSpace = (TextView) view.findViewById(R.id.usedSpace);
        usedSpace.setTypeface(typefaceThin);

        freeSpace = (TextView) view.findViewById(R.id.freeSpace);
        freeSpace.setTypeface(typefaceThin);

        PaperButton cloudFiles = (PaperButton) view.findViewById(R.id.cloudFiles);
        cloudFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filesCloudList.getVisibility() != View.VISIBLE) {
                    loadList();
                    ViewUtils.collapse(cloudContainer);
                    ViewUtils.fadeInAnimation(filesCloudList);
                } else {
                    reload();
                }
            }
        });

        PaperButton deleteAllCloudButton = (PaperButton) view.findViewById(R.id.deleteAllCloudButton);
        deleteAllCloudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DeleteAsync(getActivity(), mCallbacks,
                        DropboxFragment.this, type).execute(getFolders());
                if (cloudContainer.getVisibility() == View.GONE) {
                    isDeleted = true;
                }
                reload();
            }
        });

        filesCloudList = (RecyclerView) view.findViewById(R.id.filesCloudList);
        filesCloudList.setLayoutManager(new LinearLayoutManager(getActivity()));
        filesCloudList.setItemAnimator(new DefaultItemAnimator());

        if (type == BackupsFragment.LOCAL_INT) {
            card1.setVisibility(View.GONE);
            card2.setVisibility(View.GONE);
        }

        new UserInfoAsync(getActivity(), type, this).execute();
        return view;
    }

    private String[] getFolders() {
        if (type == BackupsFragment.DROPBOX_INT) {
            String r = MemoryUtil.getDRDir().getPath();
            String n = MemoryUtil.getDNDir().getPath();
            String g = MemoryUtil.getDGroupsDir().getPath();
            String b = MemoryUtil.getDBDir().getPath();
            return new String[]{r, n, g, b};
        } else if (type == BackupsFragment.GOOGLE_DRIVE_INT) {
            String r = MemoryUtil.getGRDir().getPath();
            String n = MemoryUtil.getGNDir().getPath();
            String g = MemoryUtil.getGGroupsDir().getPath();
            String b = MemoryUtil.getGBDir().getPath();
            return new String[]{r, n, g, b};
        } else {
            String r = MemoryUtil.getRDir().getPath();
            String n = MemoryUtil.getNDir().getPath();
            String g = MemoryUtil.getGroupsDir().getPath();
            String b = MemoryUtil.getBDir().getPath();
            return new String[]{r, n, g, b};
        }
    }

    private void reload() {
        ViewUtils.fadeOutAnimation(filesCloudList);
        if (isDeleted) {
            new UserInfoAsync(getActivity(), type, this).execute();
        } else {
            ViewUtils.expand(cloudContainer);
            isDeleted = false;
        }
    }

    private void loadList(){
        String where = Constants.DIR_SD;
        if (type == BackupsFragment.DROPBOX_INT)
            where = Constants.DIR_SD_DBX_TMP;
        else if (type == BackupsFragment.GOOGLE_DRIVE_INT)
            where = Constants.DIR_SD_GDRIVE_TMP;

        provider = new FileDataProvider(getActivity(), where);
        FileRecyclerAdapter adapter = new FileRecyclerAdapter(getActivity(), provider.getData());
        adapter.setEventListener(this);
        filesCloudList.setAdapter(adapter);
    }

    private void fillInfo(UserModel model){
        String name = model.name;
        if (name != null) {
            cloudUser.setText(name);
        }
        String photoLink = model.photo;
        if (photoLink != null) {
            loadImage(photoLink);
        }

        long quota = model.quota;
        if (quota != 0) {
            final long availQ = quota - (model.used);
            final float free = (int) ((availQ * 100.0f) / quota);
            final float used = (int) ((model.used * 100.0f) / quota);

            cloudContainer.setVisibility(View.VISIBLE);
            usedSizeGraph.removeSlices();
            PieSlice slice = new PieSlice();
            slice.setTitle(String.format(getString(R.string.used_x), used));
            slice.setColor(ViewUtils.getColor(getActivity(), R.color.redPrimary));
            slice.setValue(used);
            usedSizeGraph.addSlice(slice);
            slice = new PieSlice();
            slice.setTitle(String.format(getString(R.string.available_x), free));
            slice.setColor(ViewUtils.getColor(getActivity(), R.color.greenPrimary));
            slice.setValue(free);
            usedSizeGraph.addSlice(slice);
            usedSizeGraph.setOnSliceClickedListener(new PieGraph.OnSliceClickedListener() {
                @Override
                public void onClick(int index) {
                    if (index == 0) {
                        mCallbacks.showSnackbar(R.string.used_x);
                    } else {
                        mCallbacks.showSnackbar(R.string.available_x);
                    }
                }
            });

            usedSpace.setText(String.format(getString(R.string.used_x),
                    MemoryUtil.humanReadableByte(model.used, false)));
            freeSpace.setText(String.format(getString(R.string.available_x),
                    MemoryUtil.humanReadableByte(availQ, false)));
        }

        cloudCount.setText(String.valueOf(model.count));

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                usedSizeGraph.animate();
            }
        }, 500);
    }

    private void loadImage(final String photoLink) {
        File dir = MemoryUtil.getImagesDir();
        File image = new File(dir, FILE_NAME);
        if (image.exists()) {
            Picasso.with(getActivity()).load(image).into(userPhoto);
            userPhoto.setVisibility(View.VISIBLE);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap bitmap = Picasso.with(getActivity())
                                .load(photoLink)
                                .get();
                        try {
                            File dir = MemoryUtil.getImagesDir();
                            File image = new File(dir, FILE_NAME);
                            if (image.createNewFile()) {
                                FileOutputStream stream = new FileOutputStream(image);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                stream.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            File dir = MemoryUtil.getImagesDir();
                            File image = new File(dir, FILE_NAME);
                            if (image.exists()) {
                                Picasso.with(getActivity()).load(image).into(userPhoto);
                                userPhoto.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationCallbacks.");
        }
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_GROUPS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void editFile(int position) {
        FileModel model = provider.getItem(position);
        String fileName = model.getFileName();
        if (fileName.endsWith(FileConfig.FILE_NAME_REMINDER)) {
            startActivity(new Intent(getActivity(),
                    ReminderManager.class).putExtra(Constants.EDIT_PATH,
                    provider.getItem(position).getFilePath()));
        } else if (fileName.endsWith(FileConfig.FILE_NAME_NOTE)) {
            startActivity(new Intent(getActivity(),
                    NotesManager.class).putExtra(Constants.EDIT_PATH,
                    provider.getItem(position).getFilePath()));
        } else if (fileName.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
            startActivity(new Intent(getActivity(),
                    AddBirthday.class).putExtra(Constants.EDIT_PATH,
                    provider.getItem(position).getFilePath()));
        } else if (fileName.endsWith(FileConfig.FILE_NAME_GROUP)) {
            startActivity(new Intent(getActivity(),
                    CategoryManager.class).putExtra(Constants.EDIT_PATH,
                    provider.getItem(position).getFilePath()));
        }
    }

    @Override
    public void onItemClicked(int position, View view) {
        editFile(position);
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(getActivity(), new LCAMListener() {
            @Override
            public void onAction(int item) {
                if (item == 0) {
                    editFile(position);
                }
                if (item == 1) {
                    new DeleteAsync(getActivity(), mCallbacks, DropboxFragment.this, type)
                            .execute(provider.getItem(position).getFilePath());
                    reload();
                }
            }
        }, items);
    }

    @Override
    public void endExecution(boolean result) {
        isDeleted = true;
        loadList();
    }

    @Override
    public void onReceive(UserModel model) {
        fillInfo(model);
    }
}
