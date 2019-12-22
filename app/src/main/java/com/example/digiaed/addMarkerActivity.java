package com.example.digiaed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class addMarkerActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = addMarkerActivity.class.getName();

    private TextView textPhoto,textAddress;
    private EditText textDescr,textName;
    private ProgressBar progressBar3;
    private Button addAed;
    private ImageView imgAdd;
    private GoogleMap mMap;
    private Intent intent;
    protected Location lastLocation;

    private AddressResultReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);

        //Call async intent to get location
        intent = getIntent();
        double lat = intent.getDoubleExtra("Lat",0.00);
        double lon = intent.getDoubleExtra("Lon",0.00);

        startIntentService(lat,lon);

        //******************
        //GET XML VARIABLES
        //******************
        textPhoto = (TextView) findViewById(R.id.textPhoto);
        textAddress = (TextView) findViewById(R.id.textAedAdr);
        textDescr = (EditText) findViewById(R.id.textAedDescr);
        textName = (EditText) findViewById(R.id.textAedName);
        progressBar3 = (ProgressBar) findViewById(R.id.progressBar3);
        addAed = (Button) findViewById(R.id.btnAddAed);
        imgAdd = (ImageView) findViewById(R.id.imgAdd);



    }

    protected void startIntentService(double lat, double lon){
        Intent addressIntent = new Intent(addMarkerActivity.this,FetchAddressIntentService.class);
        addressIntent.putExtra(Constants.RECEIVER,resultReceiver);
        addressIntent.putExtra("Lat",lat);
        addressIntent.putExtra("Lon",lon);
        startService(addressIntent);
    }

    public void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //******************
        //SET MAP
        //******************
        mMap = googleMap;



        //String address = addresses.get(0).getAddressLine(0); //0 to obtain first possible address
        //String city = addresses.get(0).getLocality();
        //String state = addresses.get(0).getAdminArea();
        //String country = addresses.get(0).getCountryName();
        //String postalCode = addresses.get(0).getPostalCode();

        //String title = address + ", " + city + ", " + state;

        // Add a marker in Sydney and move the camera
        //LatLng markerInfo = new LatLng(lat, lon);
        //mMap.addMarker(new MarkerOptions().position(markerInfo).title(title));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerInfo, 17f));


        //******************
        //SET ADDRESS TEXTVIEW
        //******************
        //textAddress.setText(title + "\n" + country + ", " + postalCode);





    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }
            //displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(addMarkerActivity.this, addressOutput, Toast.LENGTH_LONG).show();
            }

        }
    }


}
