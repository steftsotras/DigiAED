package com.example.digiaed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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

public class ShowReportActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = ShowReportActivity.class.getName();

    private GoogleMap mMap;

    private List<Map<String,Object>> markerMap;
    private List<Map<String,Object>> reportMap;

    private double lat;
    private double lon;

    private Intent intent;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_report);

        db = FirebaseFirestore.getInstance();

        intent = getIntent();
        lat = intent.getDoubleExtra("Lat",0.00);
        lon = intent.getDoubleExtra("Lon",0.00);

        markerMap = new ArrayList<Map<String,Object>>();
        reportMap = new ArrayList<Map<String,Object>>();

        initMap();

    }

    private void initMap(){

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_report);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),13f));

        getDataFromDatabase();

    }

    private void getDataFromDatabase(){

        db.collection("AEDReport")
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
                                //Log.d(TAG, "List object : "+markerEntry);
                                markerMap.add(markerEntry);

                            }

                            db.collection("AEDMap")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    //Log.d(TAG, document.getId() + " => " + document.getData());

                                                    //Get data and save on list
                                                    for(int i=0;i<markerMap.size();i++){
                                                        if(markerMap.get(i).containsValue(document.getId())){
                                                            markerMap.get(i).put("AEDName",document.get("Name"));
                                                            markerMap.get(i).put("AEDGeo",document.get("Geolocation"));

                                                            //Log.d(TAG, "List object AED : "+);
                                                        }

                                                    }
                                                }

                                                Log.d(TAG, "ALL AED REPORTS : "+markerMap);
                                                setMarkersOnMap();

                                            } else {
                                                Log.w(TAG, "Error getting documents.", task.getException());
                                            }
                                        }
                                    });

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


    }

    private void setMarkersOnMap(){

        if(!markerMap.isEmpty()){
            for(int i=0; i<markerMap.size(); i++){
                String comment = (String) markerMap.get(i).get("Comment");
                String type = (String) markerMap.get(i).get("Type");
                String name = (String) markerMap.get(i).get("AEDName");
                GeoPoint geoPoint = (GeoPoint) markerMap.get(i).get("AEDGeo");
                String id = (String) markerMap.get(i).get("Id");

                Log.d(TAG,"name: "+name+" type: "+type+" geolat: "+geoPoint.getLatitude()+" geolon: "+geoPoint.getLongitude()+ " comment: "+comment);

                String snippet = "Problem: '"+type+"' Notes: '"+comment+"'";

                MarkerOptions options = new MarkerOptions().position(new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude()))
                        .title(name)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.reportmap));

                mMap.addMarker(options).setTag(id);
            }
        }
        else{
            Log.d(TAG,"markeMap is Empty");
        }

    }




}
