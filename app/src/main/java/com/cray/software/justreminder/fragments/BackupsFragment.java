package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.adapters.FileRecyclerAdapter;
import com.cray.software.justreminder.cloud.AccountInfo;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.DropboxQuota;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.FileDataProvider;
import com.cray.software.justreminder.datas.models.FileModel;
import com.cray.software.justreminder.graph.PieGraph;
import com.cray.software.justreminder.graph.PieSlice;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.spinner.SpinnerItem;
import com.cray.software.justreminder.spinner.TitleNavigationAdapter;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.PaperButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BackupsFragment extends Fragment implements AdapterView.OnItemSelectedListener, SimpleListener {

    public static final int LOCAL_INT = 120;
    public static final int DROPBOX_INT = 121;
    public static final int GOOGLE_DRIVE_INT = 122;

    private SharedPrefs sPrefs;
    private DropboxHelper dbx = new DropboxHelper(getActivity());
    private ProgressDialog pd;
    private ColorSetter cSetter;
    private GDriveHelper gdx;

    private ArrayList<Item> navIds = new ArrayList<>();

    private LinearLayout localLayout, cloudLayout, container, cloudContainer, googleContainer, googleLayout;
    private TextView localCount;
    private TextView cloudUser;
    private TextView cloudCount;
    private TextView usedSpace;
    private TextView freeSpace;
    private TextView googleSpace;
    private TextView googleFreeSpace;
    private TextView googleCount;
    private RecyclerView filesList, filesCloudList, filesGoogleList;
    private PieGraph usedSizeGraph;

    private Typeface typefaceMedium;
    private Typeface typefaceThin;
    private boolean isDropboxDeleted = false;
    private boolean isGoogleDeleted = false;

    private Toolbar toolbar;
    private Spinner spinner;
    private View rootView;

    private FileRecyclerAdapter adapter;
    private FileDataProvider provider;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    public static BackupsFragment newInstance() {
        return new BackupsFragment();
    }

    public BackupsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.backup_manager_layout, container, false);

        cSetter = new ColorSetter(getActivity());
        sPrefs = new SharedPrefs(getActivity());

        typefaceMedium = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Medium.ttf");
        typefaceThin = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        spinner = (Spinner) inflater.inflate(R.layout.spinner, null);
        toolbar.addView(spinner);

        setNavigation();

        clearForm(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerFragment.NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_BACKUPS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        dbx = new DropboxHelper(getActivity());
        if (!dbx.isLinked()) {
            if (dbx.checkLink()) {
                setNavigation();
            } else {
                setNavigation();
            }
        }

        sPrefs = new SharedPrefs(getActivity());
    }

    private void setNavigation(){
        navIds.clear();
        sPrefs = new SharedPrefs(getActivity());
        boolean isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        if (isDark) {
            navIds.add(new Item(new SpinnerItem(getString(R.string.local_list_item), R.drawable.ic_sd_storage_white_24dp), LOCAL_INT, R.drawable.ic_sd_storage_white_24dp));
        } else {
            navIds.add(new Item(new SpinnerItem(getString(R.string.local_list_item), R.drawable.ic_sd_storage_black_24dp), LOCAL_INT, R.drawable.ic_sd_storage_white_24dp));
        }
        dbx = new DropboxHelper(getActivity());
        dbx.startSession();
        if (dbx.isLinked()){
            if (isDark) {
                navIds.add(new Item(new SpinnerItem(getString(R.string.dropbox), R.drawable.dropbox_icon_white), DROPBOX_INT, R.drawable.dropbox_icon_white));
            } else {
                navIds.add(new Item(new SpinnerItem(getString(R.string.dropbox), R.drawable.dropbox_icon), DROPBOX_INT, R.drawable.dropbox_icon_white));
            }
        }
        gdx = new GDriveHelper(getActivity());
        if (gdx.isLinked()) {
            if (isDark) {
                navIds.add(new Item(new SpinnerItem(getString(R.string.google_drive_title), R.drawable.gdrive_icon_white), GOOGLE_DRIVE_INT, R.drawable.gdrive_icon_white));
            } else {
                navIds.add(new Item(new SpinnerItem(getString(R.string.google_drive_title), R.drawable.gdrive_icon), GOOGLE_DRIVE_INT, R.drawable.gdrive_icon_white));
            }
        }

        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        for (Item item : navIds){
            navSpinner.add(item.getSpinnerItem());
        }

        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getActivity(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void attachDropbox() {
        cloudContainer = (LinearLayout) rootView.findViewById(R.id.cloudContainer);
        cloudContainer.setVisibility(View.VISIBLE);

        cloudLayout = (LinearLayout) rootView.findViewById(R.id.cloudLayout);
        cloudLayout.setVisibility(View.VISIBLE);

        CardView card1 = (CardView) rootView.findViewById(R.id.card1);
        CardView card2 = (CardView) rootView.findViewById(R.id.card2);
        CardView card3 = (CardView) rootView.findViewById(R.id.card3);
        card1.setCardBackgroundColor(cSetter.getCardStyle());
        card2.setCardBackgroundColor(cSetter.getCardStyle());
        card3.setCardBackgroundColor(cSetter.getCardStyle());
        if (Module.isLollipop()) {
            card3.setCardElevation(Configs.CARD_ELEVATION);
            card2.setCardElevation(Configs.CARD_ELEVATION);
            card1.setCardElevation(Configs.CARD_ELEVATION);
        }

        cloudUser = (TextView) rootView.findViewById(R.id.cloudUser);
        cloudUser.setTypeface(typefaceThin);

        usedSizeGraph = (PieGraph) rootView.findViewById(R.id.usedSizeGraph);

        TextView cloudText = (TextView) rootView.findViewById(R.id.cloudText);
        cloudText.setTypeface(typefaceThin);

        cloudCount = (TextView) rootView.findViewById(R.id.cloudCount);
        cloudCount.setTypeface(typefaceMedium);

        usedSpace = (TextView) rootView.findViewById(R.id.usedSpace);
        usedSpace.setTypeface(typefaceThin);

        freeSpace = (TextView) rootView.findViewById(R.id.freeSpace);
        freeSpace.setTypeface(typefaceThin);

        PaperButton cloudFiles = (PaperButton) rootView.findViewById(R.id.cloudFiles);
        cloudFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filesCloudList.getVisibility() != View.VISIBLE) {
                    loadDropboxList();
                    ViewUtils.collapse(cloudContainer);
                    ViewUtils.fadeInAnimation(filesCloudList);
                } else {
                    reloadDropbox();
                }
            }
        });

        PaperButton deleteAllCloudButton = (PaperButton) rootView.findViewById(R.id.deleteAllCloudButton);
        deleteAllCloudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFilesInFolder(Constants.DIR_SD_DBX_TMP);
                if (cloudContainer.getVisibility() == View.GONE) {
                    isDropboxDeleted = true;
                }
                reloadDropbox();
            }
        });

        filesCloudList = (RecyclerView) rootView.findViewById(R.id.filesCloudList);
        pd = ProgressDialog.show(getActivity(), null, getString(R.string.receiving_data_text), false);
        cloudContainer.setVisibility(View.GONE);
        loadDropboxList();
        loadInfo(pd);
    }

    private void removeFilesInFolder(String folder) {
        File file = new File(folder);
        if (file.exists()) {
            if (file.isDirectory()) {
                provider = new FileDataProvider(getActivity(), folder);
                ArrayList<FileModel> list = provider.getData();
                if (list != null && list.size() > 0) {
                    for (FileModel model : list) {
                        File file1 = new File(model.getFilePath());
                        if (file1.exists()) {
                            file1.delete();
                        }
                    }
                }
                if (mCallbacks != null) {
                    mCallbacks.showSnackbar(R.string.all_files_removed);
                }
            } else {
                file.delete();
                if (mCallbacks != null) {
                    mCallbacks.showSnackbar(R.string.file_delted);
                }
            }
        }

    }

    private void reloadDropbox() {
        ViewUtils.fadeOutAnimation(filesCloudList);
        if (isDropboxDeleted) {
            pd = ProgressDialog.show(getActivity(), null, getString(R.string.receiving_data_text), false);
            loadInfo(pd);
        } else {
            ViewUtils.expand(cloudContainer);
            isDropboxDeleted = false;
        }
    }

    private void loadDropboxList(){
        provider = new FileDataProvider(getActivity(), Constants.DIR_SD_DBX_TMP);
        adapter = new FileRecyclerAdapter(getActivity(), provider.getData());
        adapter.setEventListener(this);
        filesCloudList.setLayoutManager(new LinearLayoutManager(getActivity()));
        filesCloudList.setAdapter(adapter);
        filesCloudList.setItemAnimator(new DefaultItemAnimator());
    }

    private void deleteFile(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            removeFilesInFolder(filePath);
            pd = ProgressDialog.show(getActivity(), null, getString(R.string.deleting), false);
            deleteFromDropbox(file.getName(), pd);
            isDropboxDeleted = true;
        }
    }

    private void deleteFromDropbox(final String name, final ProgressDialog progress){
        new Thread(new Runnable() {
            @Override
            public void run() {
                dbx = new DropboxHelper(getActivity());
                dbx.startSession();
                if (dbx.isLinked()){
                    if (SyncHelper.isConnected(getActivity())){
                        dbx.deleteReminder(name);
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        loadDropboxList();
                    }
                });
            }
        }).start();
    }

    private void attachGoogleDrive() {
        googleContainer = (LinearLayout) rootView.findViewById(R.id.googleContainer);
        googleContainer.setVisibility(View.VISIBLE);

        googleLayout = (LinearLayout) rootView.findViewById(R.id.googleLayout);
        googleLayout.setVisibility(View.VISIBLE);

        CardView card4 = (CardView) rootView.findViewById(R.id.card4);
        CardView card5 = (CardView) rootView.findViewById(R.id.card5);
        CardView card6 = (CardView) rootView.findViewById(R.id.card6);
        card4.setCardBackgroundColor(cSetter.getCardStyle());
        card5.setCardBackgroundColor(cSetter.getCardStyle());
        card6.setCardBackgroundColor(cSetter.getCardStyle());
        if (Module.isLollipop()) {
            card4.setCardElevation(Configs.CARD_ELEVATION);
            card5.setCardElevation(Configs.CARD_ELEVATION);
            card6.setCardElevation(Configs.CARD_ELEVATION);
        }

        TextView googleUser = (TextView) rootView.findViewById(R.id.googleUser);
        googleUser.setTypeface(typefaceThin);

        TextView googleText = (TextView) rootView.findViewById(R.id.googleText);
        googleText.setTypeface(typefaceThin);

        googleCount = (TextView) rootView.findViewById(R.id.googleCount);
        googleCount.setTypeface(typefaceMedium);

        googleSpace = (TextView) rootView.findViewById(R.id.googleSpace);
        googleSpace.setTypeface(typefaceThin);

        googleFreeSpace = (TextView) rootView.findViewById(R.id.googleFreeSpace);
        googleFreeSpace.setTypeface(typefaceThin);

        PaperButton googleFiles = (PaperButton) rootView.findViewById(R.id.googleFiles);
        googleFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filesGoogleList.getVisibility() != View.VISIBLE) {
                    loadGoogleList();
                    ViewUtils.collapse(googleContainer);
                    ViewUtils.fadeInAnimation(filesGoogleList);
                } else {
                    reloadGoogle();
                }
            }
        });

        PaperButton googleDeleteAllCloudButton = (PaperButton) rootView.findViewById(R.id.googleDeleteAllCloudButton);
        googleDeleteAllCloudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFilesInFolder(Constants.DIR_SD_GDRIVE_TMP);
                if (googleContainer.getVisibility() == View.GONE) {
                    isGoogleDeleted = true;
                }
                reloadGoogle();
            }
        });

        filesGoogleList = (RecyclerView) rootView.findViewById(R.id.filesGoogleList);
        pd = ProgressDialog.show(getActivity(), null, getString(R.string.receiving_data_text), false);
        googleContainer.setVisibility(View.GONE);
        loadGoogleInfo(pd);
    }

    private void reloadGoogle() {
        ViewUtils.fadeOutAnimation(filesGoogleList);
        if (isGoogleDeleted) {
            pd = ProgressDialog.show(getActivity(), null, getString(R.string.receiving_data_text), false);
            loadGoogleInfo(pd);
        } else {
            ViewUtils.expand(googleContainer);
            isGoogleDeleted = false;
        }
    }

    private void loadGoogleList(){
        provider = new FileDataProvider(getActivity(), Constants.DIR_SD_GDRIVE_TMP);
        adapter = new FileRecyclerAdapter(getActivity(), provider.getData());
        adapter.setEventListener(this);
        filesGoogleList.setLayoutManager(new LinearLayoutManager(getActivity()));
        filesGoogleList.setAdapter(adapter);
        filesGoogleList.setItemAnimator(new DefaultItemAnimator());
    }

    private void deleteGoogleFile(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            removeFilesInFolder(filePath);
            pd = ProgressDialog.show(getActivity(), null, getString(R.string.deleting), false);
            deleteFromGoogle(file.getName(), pd);
            if (mCallbacks != null) {
                mCallbacks.showSnackbar(R.string.file_delted);
            }
            isGoogleDeleted = true;
        }
    }

    private void deleteFromGoogle(final String name, final ProgressDialog progress){
        new Thread(new Runnable() {
            @Override
            public void run() {
                gdx = new GDriveHelper(getActivity());
                if (gdx.isLinked()){
                    if (SyncHelper.isConnected(getActivity())){
                        gdx.deleteReminder(name);
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progress != null && progress.isShowing()) {
                            progress.dismiss();
                        }
                        loadGoogleList();
                    }
                });
            }
        }).start();
    }

    private void loadGoogleInfo(final ProgressDialog progressDialog){
        new Thread(new Runnable() {
            @Override
            public void run() {
                gdx = new GDriveHelper(getActivity());
                try {
                    gdx.downloadReminder();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final int count = gdx.countFiles();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        googleContainer.setVisibility(View.VISIBLE);
                        googleCount.setText(String.valueOf(count));
                    }
                });
            }
        }).start();
    }

    private void loadInfo(final ProgressDialog progressDialog){
        new Thread(new Runnable() {
            @Override
            public void run() {
                dbx = new DropboxHelper(getActivity());
                dbx.downloadReminder();

                String name = null;
                try {
                    name = new AccountInfo(getActivity()).execute().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                final String finalName = name;

                Long[] res = new Long[0];
                try {
                    res = new DropboxQuota(getActivity()).execute().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                long quota = res[0], quotaNormal = res[1], quotaShared = res[2];

                final long usedQ = quotaNormal + quotaShared;
                final long availQ = quota - (quotaNormal + quotaShared);

                final float free = (int)((availQ * 100.0f) / quota);
                final float used = (int)((usedQ * 100.0f) / quota);

                final int count = new DropboxHelper(getActivity()).countFiles();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        cloudContainer.setVisibility(View.VISIBLE);

                        cloudUser.setText(finalName);
                        usedSizeGraph.removeSlices();
                        PieSlice slice = new PieSlice();
                        slice.setTitle(getString(R.string.used_text) + " " + used);
                        slice.setColor(getResources().getColor(R.color.redPrimary));
                        slice.setValue(used);
                        usedSizeGraph.addSlice(slice);
                        slice = new PieSlice();
                        slice.setTitle(getString(R.string.available_text) + " " + free);
                        slice.setColor(getResources().getColor(R.color.greenPrimary));
                        slice.setValue(free);
                        usedSizeGraph.addSlice(slice);

                        usedSpace.setText(getString(R.string.used_text) + " " + humanReadableByteCount(usedQ, false));
                        freeSpace.setText(getString(R.string.available_text) + " " + humanReadableByteCount(availQ, false));

                        cloudCount.setText(String.valueOf(count));

                        usedSizeGraph.animate();
                    }
                });
            }
        }).start();
    }

    private void attachLocal() {
        container = (LinearLayout) rootView.findViewById(R.id.container);
        container.setVisibility(View.VISIBLE);

        localLayout = (LinearLayout) rootView.findViewById(R.id.localLayout);
        localLayout.setVisibility(View.VISIBLE);

        CardView card7 = (CardView) rootView.findViewById(R.id.card7);
        card7.setCardBackgroundColor(cSetter.getCardStyle());
        if (Module.isLollipop()) {
            card7.setCardElevation(Configs.CARD_ELEVATION);
        }

        localCount = (TextView) rootView.findViewById(R.id.localCount);
        localCount.setTypeface(typefaceMedium);

        TextView backupText = (TextView) rootView.findViewById(R.id.backupText);
        backupText.setTypeface(typefaceThin);

        PaperButton backupFilesText = (PaperButton) rootView.findViewById(R.id.backupFilesText);
        backupFilesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filesList.getVisibility() != View.VISIBLE) {
                    loadLocalList();
                    ViewUtils.collapse(container);
                    ViewUtils.fadeInAnimation(filesList);
                } else {
                    reloadLocal();
                }
            }
        });

        PaperButton deleteAllButton = (PaperButton) rootView.findViewById(R.id.deleteAllButton);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFilesInFolder(Constants.DIR_SD);
                if (container.getVisibility() == View.GONE) {
                    ViewUtils.fadeOutAnimation(filesList);
                    ViewUtils.expand(container);
                    showFilesCount();
                }
                reloadLocal();

            }
        });
        showFilesCount();
        filesList = (RecyclerView) rootView.findViewById(R.id.filesList);
    }

    private void reloadLocal() {
        ViewUtils.fadeOutAnimation(filesList);
        ViewUtils.expand(container);
        showFilesCount();
    }

    private void loadLocalList(){
        provider = new FileDataProvider(getActivity(), Constants.DIR_SD);
        adapter = new FileRecyclerAdapter(getActivity(), provider.getData());
        adapter.setEventListener(this);
        filesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        filesList.setAdapter(adapter);
        filesList.setItemAnimator(new DefaultItemAnimator());
    }

    private void showFilesCount() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
            File[] files = sdPathDr.listFiles();
            if (files != null) {
                localCount.setText(String.valueOf(files.length));
            } else {
                localCount.setText("0");
            }
        } else {
            localCount.setText("0");
        }
    }

    private void deleteLocalFile(String filePath) {
        if (filePath != null) {
            removeFilesInFolder(filePath);
            loadLocalList();
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void clearForm(View rootView) {
        localLayout = (LinearLayout) rootView.findViewById(R.id.localLayout);
        localLayout.setVisibility(View.GONE);
        cloudLayout = (LinearLayout) rootView.findViewById(R.id.cloudLayout);
        cloudLayout.setVisibility(View.GONE);
    }

    private boolean isLocal(){
        localLayout = (LinearLayout) rootView.findViewById(R.id.localLayout);
        return localLayout.getVisibility() == View.VISIBLE;
    }

    private boolean isDropbox(){
        cloudLayout = (LinearLayout) rootView.findViewById(R.id.cloudLayout);
        return cloudLayout.getVisibility() == View.VISIBLE;
    }

    private boolean isGoogleDrive(){
        googleLayout = (LinearLayout) rootView.findViewById(R.id.googleLayout);
        return googleLayout.getVisibility() == View.VISIBLE;
    }

    private void detachLocal(){
        localLayout = (LinearLayout) rootView.findViewById(R.id.localLayout);
        ViewUtils.collapse(localLayout);
    }

    private void detachDropbox(){
        cloudLayout = (LinearLayout) rootView.findViewById(R.id.cloudLayout);
        ViewUtils.collapse(cloudLayout);
        usedSpace.setText("");
        freeSpace.setText("");
        cloudCount.setText("");
    }

    private void detachGoogleDrive(){
        googleLayout = (LinearLayout) rootView.findViewById(R.id.googleLayout);
        ViewUtils.collapse(googleLayout);
        googleSpace.setText("");
        googleFreeSpace.setText("");
        googleCount.setText("");
    }

    private void detachLayout(){
        if (isLocal()) {
            detachLocal();
        }
        if (isDropbox()) {
            detachDropbox();
        }
        if (isGoogleDrive()) {
            detachGoogleDrive();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position >= navIds.size()) {
            return;
        }
        final Item item = navIds.get(position);
        if (item.getId() == LOCAL_INT){
            detachLayout();
            attachLocal();
            toolbar.setLogo(item.getLogo());
        } else {
            if (item.getId() == DROPBOX_INT){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean isC = SyncHelper.isConnected(getActivity());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isC) {
                                    detachLayout();
                                    attachDropbox();
                                    toolbar.setLogo(item.getLogo());
                                } else {
                                    spinner.setSelection(0);
                                }
                            }
                        });
                    }
                }).start();
            }
            if (item.getId() == GOOGLE_DRIVE_INT){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean isC = SyncHelper.isConnected(getActivity());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isC) {
                                    detachLayout();
                                    attachGoogleDrive();
                                    toolbar.setLogo(item.getLogo());
                                } else {
                                    spinner.setSelection(0);
                                }
                            }
                        });
                    }
                }).start();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void actionDelete(int position){
        if (provider.getWhere().matches(Constants.FilesConstants.FILE_TYPE_DROPBOX)){
            deleteFile(provider.getItem(position).getFilePath());
            reloadDropbox();
        }
        if (provider.getWhere().matches(Constants.FilesConstants.FILE_TYPE_GDRIVE)){
            deleteGoogleFile(provider.getItem(position).getFilePath());
            reloadGoogle();
        }
        if (provider.getWhere().matches(Constants.FilesConstants.FILE_TYPE_LOCAL)){
            deleteLocalFile(provider.getItem(position).getFilePath());
            reloadLocal();
        }
    }

    @Override
    public void onItemClicked(int position, View view) {
        startActivity(new Intent(getActivity(),
                ReminderManager.class).putExtra(Constants.EDIT_PATH,
                provider.getItem(position).getFilePath()));
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final CharSequence[] items = {getString(R.string.edit), getString(R.string.delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                if (item == 0) {
                    startActivity(new Intent(getActivity(),
                            ReminderManager.class).putExtra(Constants.EDIT_PATH,
                            provider.getItem(position).getFilePath()));
                }
                if (item == 1) {
                    actionDelete(position);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public class Item {
        private SpinnerItem spinnerItem;
        private int id, logo;

        public Item(SpinnerItem spinnerItem, int id, int logo){
            this.spinnerItem = spinnerItem;
            this.id = id;
            this.logo = logo;
        }

        public int getLogo() {
            return logo;
        }

        public int getId() {
            return id;
        }

        public SpinnerItem getSpinnerItem() {
            return spinnerItem;
        }
    }
}
