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

package com.cray.software.justreminder.places;

import android.os.AsyncTask;
import android.util.Log;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.interfaces.ExecutionListener;
import com.cray.software.justreminder.reminder.json.JPlaceParser;
import com.cray.software.justreminder.reminder.json.RequestBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PlacesTask extends AsyncTask<Void, Void, ArrayList<GooglePlaceItem>> {

    private ExecutionListener listener;
    private String request;
    private double lat, lng;

    public PlacesTask(ExecutionListener listener, String request, double lat, double lng) {
        this.listener = listener;
        this.request = request;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected ArrayList<GooglePlaceItem> doInBackground(Void... place) {
        String result = "";
        String query = RequestBuilder.getSearch(request);
        if (lat != 0.0 && lng != 0.0) {
            query = RequestBuilder.getNearby(lat, lng, request);
        }
        Log.d(Constants.LOG_TAG, "Request " + query);
        try {
            result = downloadUrl(query);
        } catch(Exception e) {
            Log.d("Place Task", e.toString());
        }
        ArrayList<GooglePlaceItem> places = new ArrayList<>();
        if (result != null) {
            JPlaceParser parser = new JPlaceParser();
            try {
                JSONObject jObject = new JSONObject(result);
                JSONArray jPlaces = jObject.getJSONArray("results");
                if (jPlaces.length() > 0) {
                    for (int i = 0; i < jPlaces.length(); i++) {
                        JSONObject object = jPlaces.getJSONObject(i);
                        places.add(parser.getDetails(object));
                    }
                }
            } catch(Exception e) {
                Log.d("Exception",e.toString());
            }
        }
        return places;
    }

    @Override
    protected void onPostExecute(ArrayList<GooglePlaceItem> result) {
        super.onPostExecute(result);
        if (listener != null) {
            listener.onFinish(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch(Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }
}
