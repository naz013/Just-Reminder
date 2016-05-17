/**
 * Copyright 2015 Nazar Suhovich
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
package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.utils.SuperUtil;

public class LocationType extends Type {

    private Context mContext;

    public LocationType(Context context, String type, Fragment fragment) {
        super(context);
        this.mContext = context;
        setType(type);
        inflateView(fragment);
    }

    public LocationType(Context context, String type) {
        super(context);
        this.mContext = context;
        setType(type);
    }

    @Override
    public long save(JModel item) {
        long id = super.save(item);
        startTracking(id, item);
        return id;
    }

    @Override
    public void save(long id, JModel item) {
        super.save(id, item);
        startTracking(id, item);
    }

    private void startTracking(long id, JModel item) {
        if (item.getEventTime() > 0) {
            new PositionDelayReceiver().setDelay(mContext, id);
        } else {
            if (!SuperUtil.isServiceRunning(mContext, GeolocationService.class)) {
                mContext.startService(new Intent(mContext, GeolocationService.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }
}
