package com.example.digiaed;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class FetchAddressIntentService extends IntentService {

    public FetchAddressIntentService(){
        super("FetchAddressIntentService");
    }

    protected ResultReceiver receiver;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent == null) {
            return;
        }
        String errorMessage = "";

        double lat = intent.getDoubleExtra("Lat",0.00);
        double lon = intent.getDoubleExtra("Lon",0.00);

        //receiver = intent.getParcelableExtra("RECEIVER");

        Log.d(TAG,"Lat: "+lat+"Lon: "+lon);

        Geocoder geocoder = new Geocoder(FetchAddressIntentService.this, Locale.getDefault());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1); //1 num of possible location returned

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service not available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid lat lon";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + lat +
                    ", Longitude = " +
                    lon, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No adrress found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "address found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }

    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        Log.d(TAG,message);
        receiver.send(resultCode, bundle);
    }

}


