package com.example.digiaed;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class AEDMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private float DEFAULT_ZOOM = 15f;

    private ImageView ic_plus;
    private ImageView ic_list;
    private ImageView addMarker;
    private ImageView cancel;
    private TextView textConfirm;

    private Location currentLocation;
    private double currentMarkerLat;
    private double currentMarkerLon;

    private Marker curraddmarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aedmap);
        getLocationPermission();
        ic_plus = (ImageView) findViewById(R.id.ic_plus);
        ic_list = (ImageView) findViewById(R.id.ic_layers);
        addMarker = (ImageView) findViewById(R.id.addMarker);
        cancel = (ImageView) findViewById(R.id.cancel);
        textConfirm = (TextView) findViewById(R.id.textConfirm);

        currentMarkerLat=0.00;
        currentMarkerLon=0.00;
    }

    private void initMap(){

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        //LatLng hellas = new LatLng(38.311449, 25.022821);
       // mMap.addMarker(new MarkerOptions().position(hellas).title("Marker in Aegean Sea"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hellas,5));

        //Initalize MyLocation stuff
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        if(mLocationPermissionGranted){
            getDeviceLocation();

        }

        ic_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DEFAULT_ZOOM = 17f;
                getDeviceLocation();

                //double lat = currentLocation.getLatitude();
                //double lon = currentLocation.getLongitude();
                LatLng currlatlon = new LatLng(currentLocation.getLatitude()+0.0002,currentLocation.getLongitude()+0.0002);


                curraddmarker = mMap.addMarker(new MarkerOptions().position(currlatlon).draggable(true));
                currentMarkerLat=curraddmarker.getPosition().latitude;
                currentMarkerLon=curraddmarker.getPosition().longitude;
                hideUI();

            }
        });

        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                showUI();
                addMarker.setVisibility(View.GONE);

                //double lat = curraddmarker.getPosition().latitude;
                //double lon = curraddmarker.getPosition().longitude;

                //Toast.makeText(AEDMapActivity.this, "Lat:"+currentMarkerLat+" Lon:"+currentMarkerLon, Toast.LENGTH_SHORT).show();
                Intent i =new Intent(AEDMapActivity.this, addMarkerActivity.class);
                i.putExtra("Lat",curraddmarker.getPosition().latitude);
                i.putExtra("Lon",curraddmarker.getPosition().longitude);
                startActivity(i);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showUI();
                curraddmarker.remove();

                getDeviceLocation();

            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {


            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                currentMarkerLat=curraddmarker.getPosition().latitude;
                currentMarkerLon=curraddmarker.getPosition().longitude;
            }
        });

    }

    private void hideUI(){

        ic_plus.setVisibility(View.GONE);
        ic_list.setVisibility(View.GONE);
        addMarker.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        textConfirm.setVisibility(View.VISIBLE);

    }

    private void showUI(){

        ic_plus.setVisibility(View.VISIBLE);
        ic_list.setVisibility(View.VISIBLE);
        addMarker.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        textConfirm.setVisibility(View.GONE);
        DEFAULT_ZOOM = 15f;
    }

    private void getDeviceLocation(){

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),DEFAULT_ZOOM);
                        }
                    }
                });

            }
        }catch(SecurityException e){
            Toast.makeText(AEDMapActivity.this,"unable to get current location",Toast.LENGTH_SHORT).show();
        }

    }

    private void moveCamera(LatLng latLng,float zoom){

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

    }

    private void getLocationPermission(){

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }
            else{
                ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i=0;i<grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    //intialize map
                    initMap();
                }
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();

    }
}