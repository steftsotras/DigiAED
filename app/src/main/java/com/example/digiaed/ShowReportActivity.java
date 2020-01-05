package com.example.digiaed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_report);

        db = FirebaseFirestore.getInstance();

        markerMap = new ArrayList<Map<String,Object>>();
        reportMap = new ArrayList<Map<String,Object>>();

        getDataFromDatabase();

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
                                                //setMarkersOnMap();

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




}
