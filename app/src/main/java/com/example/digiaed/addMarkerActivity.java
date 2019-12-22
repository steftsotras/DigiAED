package com.example.digiaed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.firestore.GeoPoint;

import static android.content.ContentValues.TAG;

public class addMarkerActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = addMarkerActivity.class.getName();

    private TextView textAddress;
    private EditText textDescr,textName;
    private ProgressBar progressBar3;
    private Button addAed;
    private ImageView imgAdd;
    private GoogleMap mMap;
    private Intent intent;
    protected Location lastLocation;

    private double lat;
    private double lon;

    private List<Address> addresses;

    private boolean showAddress;
    private Uri imguri=null;
    private StorageReference mStorageRef;

    private String imgUrl;
    private AddressResultReceiver resultReceiver;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);

        showAddress=false;
        imgUrl = "";

        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        db = FirebaseFirestore.getInstance();

        //Call async intent to get location
        intent = getIntent();
        lat = intent.getDoubleExtra("Lat",0.00);
        lon = intent.getDoubleExtra("Lon",0.00);



        //startIntentService(lat,lon);
        getAddress(lat,lon);
        initMap();

        //******************
        //GET XML VARIABLES
        //******************
        textAddress = (TextView) findViewById(R.id.textAedAdr);
        textDescr = (EditText) findViewById(R.id.textAedDescr);
        textName = (EditText) findViewById(R.id.textAedName);
        progressBar3 = (ProgressBar) findViewById(R.id.progressBar3);
        addAed = (Button) findViewById(R.id.btnAddAed);
        imgAdd = (ImageView) findViewById(R.id.imgAdd);

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChooser();
            }
        });

        addAed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imguri != null){
                    FileUploader();
                }

                //Fetch texts
                String AEDName = textName.getText().toString();
                String AEDDescr = textDescr.getText().toString();

                GeoPoint geoloc = new GeoPoint(lat, lon);

                //Save to Database
                Map<String, Object> aed = new HashMap<>();
                aed.put("Description",AEDDescr);
                aed.put("Geolocation",geoloc);
                aed.put("ImageUrl",imgUrl);
                aed.put("Name",AEDName);

                db.collection("AEDMap").add(aed).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

            }
        });

    }

    //Get Image Extension
    public String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    //Upload Image to Firebase Storage
    private void FileUploader(){



        StorageReference ref = mStorageRef.child(System.currentTimeMillis()+"."+getExtension(imguri));

        ref.putFile(imguri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d(TAG,"Image Upload Success, url: "+uri.toString());
                                if(uri.toString()!=null) {
                                    imgUrl = uri.toString();
                                }
                            }
                        });

                        // Get a URL to the uploaded content

                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d(TAG,"Image Failed to Upload");
                    }
                });

    }

    //Choose Picture
    public void FileChooser(){
        Intent fileintent = new Intent();
        fileintent.setType("image/'");
        fileintent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(fileintent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imguri=data.getData();
            imgAdd.setImageURI(imguri);

        }
    }


    //Geocoder Location Address
    public void getAddress(double lat, double lon){
        String errorMessage = "";
        addresses=null;

        Geocoder geocoder = new Geocoder(addMarkerActivity.this,Locale.getDefault());
        try{

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
            //deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {

            showAddress = true;
            //Toast.makeText(addMarkerActivity.this, title, Toast.LENGTH_LONG).show();
        }

    }


    //API recommended way to fetch geocoding addresses
    protected void startIntentService(double lat, double lon){
        Intent addressIntent = new Intent(addMarkerActivity.this,FetchAddressIntentService.class);
        addressIntent.putExtra(Constants.RECEIVER,resultReceiver);
        addressIntent.putExtra("Lat",lat);
        addressIntent.putExtra("Lon",lon);
        startService(addressIntent);
    }


    //Initialize Map
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

        LatLng markerInfo = new LatLng(lat, lon);
        Marker mMarker = mMap.addMarker(new MarkerOptions().position(markerInfo));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerInfo, 17f));


        //If fetching address was successfull show address data
        if(showAddress){

            String address = addresses.get(0).getAddressLine(0); //0 to obtain first possible address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();

            String title = address + ", " + city + ", " + state;

            mMarker.setTitle(title);

            textAddress.setText(title + "\n" + country + ", " + postalCode);

        }
        else{
            mMarker.setTitle("Lat: "+lat+", Lon: "+lon);
        }
    }


    //Recieve Address from Service Intent
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
