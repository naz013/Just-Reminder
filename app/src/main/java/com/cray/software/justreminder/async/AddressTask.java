package com.cray.software.justreminder.async;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class AddressTask extends AsyncTask<String, Void, List<Address>> {

    private Context mContext;

    public AddressTask(Context context){
        this.mContext = context;
    }

    @Override
    protected List<Address> doInBackground(String... locationName) {
        // Creating an instance of Geocoder class
        Geocoder geocoder = new Geocoder(mContext);
        List<Address> addresses = null;

        try {
            // Getting a maximum of 3 Address that matches the input text
            addresses = geocoder.getFromLocationName(locationName[0], 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) {
        if(addresses==null || addresses.size()==0){
            Toast.makeText(mContext, "No Location found", Toast.LENGTH_SHORT).show();
        }

    }
}