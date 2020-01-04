package com.example.digiaed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;



public class AEDMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private static final String TAG = AEDMapActivity.class.getName();

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private float DEFAULT_ZOOM = 15f;

    private ImageView ic_plus;
    private ImageView ic_acc;
    private ImageView ic_cpr;
    private ImageView ic_alert;
    private ImageView ic_info;
    private ImageView addMarker;
    private ImageView cancel;
    private TextView textConfirm;

    private TextView txt_plus;
    private TextView txt_acc;
    private TextView txt_cpr;
    private TextView txt_alert;
    private TextView txt_info;

    private Location currentLocation;
    private double currentMarkerLat;
    private double currentMarkerLon;

    private Marker curraddmarker;
    private Toolbar toolbar;
    private AppCompatDelegate delegate;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Map<String,Object>> markerMap;

    private Boolean menuUI;
    private Boolean markerInfo;

    private String marker_id;
    private String marker_name;
    private String marker_desc;
    private String marker_pic;
    private GeoPoint marker_geo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aedmap);

        markerInfo = false;
        menuUI = true;

        marker_id = "";
        marker_name = "";
        marker_desc = "";
        marker_geo = null;
        marker_pic = "";

        currentMarkerLat=0.00;
        currentMarkerLon=0.00;

        markerMap = new ArrayList<Map<String,Object>>();

        getLocationPermission();

        ic_plus = (ImageView) findViewById(R.id.ic_plus);
        ic_acc = (ImageView) findViewById(R.id.ic_acc);
        ic_cpr = (ImageView) findViewById(R.id.ic_cpr);
        ic_alert = (ImageView) findViewById(R.id.ic_alert);
        ic_info = (ImageView) findViewById(R.id.ic_info);
        addMarker = (ImageView) findViewById(R.id.addMarker);
        cancel = (ImageView) findViewById(R.id.cancel);
        textConfirm = (TextView) findViewById(R.id.textConfirm);

        txt_plus = (TextView) findViewById(R.id.txt_plus);
        txt_acc = (TextView) findViewById(R.id.txt_acc);
        txt_cpr = (TextView) findViewById(R.id.txt_cpr);
        txt_alert = (TextView) findViewById(R.id.txt_alert);
        txt_info = (TextView) findViewById(R.id.txt_info);

        hideMarkerInfoIcon();


        ic_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(AEDMapActivity.this, LoginActivity.class));

            }
        });

        ic_cpr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.youtube.com/watch?v=fb29LCjX4-E&t=81s");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {

    //MenuInflater inflater = getMenuInflater();
    //    inflater.inflate(R.menu.menu, menu);
    //    return true;

    //}

    private void getDataFromDatabase(){

        db.collection("AEDMap")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                //Get data and save on list

                                Map<String,Object> markerEntry = document.getData();
                                markerEntry.put("Id",document.getId());
                                Log.d(TAG, "List object : "+markerEntry);
                                markerMap.add(markerEntry);

                            }

                            setMarkersOnMap();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

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

        getDataFromDatabase();

        // Add a marker in Sydney and move the camera
        //LatLng hellas = new LatLng(38.311449, 25.022821);
       // mMap.addMarker(new MarkerOptions().position(hellas).title("Marker in Aegean Sea"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hellas,5));

        //Initalize MyLocation stuff
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        //setMarkersOnMap();

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
                showConfirm();

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

                hideConfirm();
                showUI();
                curraddmarker.remove();

                getDeviceLocation();

            }
        });



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng arg0)
            {
                if(menuUI){
                    if(markerInfo){
                        hideMarkerInfoIcon();
                        return;
                    }

                    menuUI = false;
                    hideUI();

                }
                else{
                    menuUI = true;
                    showUI();
                }
            }
        });



        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                showMarkerInfoIcon();

                Log.d(TAG,marker.getTag().toString());

                if(!markerMap.isEmpty()){
                    for(int i=0; i<markerMap.size(); i++) {
                        marker_id = (String) markerMap.get(i).get("Id");
                        //Log.d(TAG,id);
                        if(marker_id.equals(marker.getTag().toString())){
                            marker_name = (String) markerMap.get(i).get("Name");
                            marker_desc = (String) markerMap.get(i).get("Description");
                            marker_pic = (String) markerMap.get(i).get("ImageUrl");
                            marker_geo = (GeoPoint) markerMap.get(i).get("Geolocation");

                            Log.d(TAG,"Marker Clicked!! - name: "+marker_name+" desc: "+marker_desc+" geo: "+marker_geo+ " imgurl: "+marker_pic);

                            ic_info.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i =new Intent(AEDMapActivity.this, MarkerInfoActivity.class);
                                    i.putExtra("Id",marker_id);
                                    i.putExtra("Name",marker_name);
                                    i.putExtra("Description",marker_desc);
                                    i.putExtra("Lat",marker_geo.getLatitude());
                                    i.putExtra("Lon",marker_geo.getLongitude());
                                    i.putExtra("imgUrl",marker_pic);
                                    startActivity(i);
                                }
                            });

                            break;
                        }
                    }
                }

                return false;
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

    private void setMarkersOnMap(){

        if(!markerMap.isEmpty()){
            for(int i=0; i<markerMap.size(); i++){
                String desc = (String) markerMap.get(i).get("Description");
                String name = (String) markerMap.get(i).get("Name");
                String imgUrl = (String) markerMap.get(i).get("ImageUrl");
                GeoPoint geoPoint = (GeoPoint) markerMap.get(i).get("Geolocation");
                String id = (String) markerMap.get(i).get("Id");

                Log.d(TAG,"name: "+name+" desc: "+desc+" geo: "+geoPoint+ " imgurl: "+imgUrl);

                String snippet = "'"+desc+"'";

                MarkerOptions options = new MarkerOptions().position(new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude()))
                                                            .title(name)
                                                           .snippet(snippet)
                                                           .icon(BitmapDescriptorFactory.fromResource(R.drawable.aedmap6));

                mMap.addMarker(options).setTag(id);
            }
        }
        else{
            Log.d(TAG,"markeMap is Empty");
        }

    }

    private void showMarkerInfoIcon(){

        txt_info.setVisibility(View.VISIBLE);
        ic_info.setVisibility(View.VISIBLE);

        markerInfo = true;

    }

    private void hideMarkerInfoIcon(){

        txt_info.setVisibility(View.GONE);
        ic_info.setVisibility(View.GONE);

        markerInfo = false;

    }

    private void hideUI(){

        ic_plus.setVisibility(View.GONE);
        ic_acc.setVisibility(View.GONE);
        ic_cpr.setVisibility(View.GONE);
        ic_alert.setVisibility(View.GONE);
        ic_info.setVisibility(View.GONE);

        txt_plus.setVisibility(View.GONE);
        txt_acc.setVisibility(View.GONE);
        txt_cpr.setVisibility(View.GONE);
        txt_alert.setVisibility(View.GONE);
        txt_info.setVisibility(View.GONE);

        markerInfo = false;
    }

    private void showConfirm(){

        addMarker.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        textConfirm.setVisibility(View.VISIBLE);
    }

    private void showUI(){

        ic_plus.setVisibility(View.VISIBLE);
        ic_acc.setVisibility(View.VISIBLE);
        ic_cpr.setVisibility(View.VISIBLE);
        ic_alert.setVisibility(View.VISIBLE);

        txt_plus.setVisibility(View.VISIBLE);
        txt_acc.setVisibility(View.VISIBLE);
        txt_cpr.setVisibility(View.VISIBLE);
        txt_alert.setVisibility(View.VISIBLE);
    }

    private void hideConfirm(){

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
