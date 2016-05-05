package com.cray.software.justreminder.apps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.cray.software.justreminder.R;

import java.util.ArrayList;
import java.util.List;

public class AppsAsync extends AsyncTask<Void, Void, Void> {

    private ProgressDialog mDialog;
    private List<AppData> mList;

    private Context mContext;
    private LoadListener mListener;

    public AppsAsync(Context context, LoadListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.please_wait), true);
    }

    @Override
    protected Void doInBackground(Void... params) {
        mList = new ArrayList<>();
        mList.clear();
        final PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            String name = packageInfo.loadLabel(pm).toString();
            String packageName = packageInfo.packageName;
            Drawable drawable = packageInfo.loadIcon(pm);
            AppData data = new AppData(name, packageName, drawable);
            int pos = getPosition(name);
            if (pos == -1) mList.add(data);
            else mList.add(getPosition(name), data);
        }
        return null;
    }

    private int getPosition(String name) {
        if (mList.size() == 0) return 0;
        int position = -1;
        for (AppData data : mList) {
            int comp = name.compareTo(data.getName());
            if (comp <= 0) {
                position = mList.indexOf(data);
                break;
            }
        }
        return position;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();

        if (mListener != null) {
            mListener.onLoaded(mList);
        }
    }
}
