package com.example.digiaed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MarkerInfoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MarkerInfoActivity.class.getName();

    private EditText textName2;
    private EditText textDescr2;
    private ImageView ic_pic;
    private ProgressBar progressBar8;
    private TextView textAddress2;
    private Button editAed;
    private ImageView imgAdd2;

    private GoogleMap mMap;
    private Intent intent;

    private double lat;
    private double lon;
    private String marker_id;
    private String marker_name;
    private String marker_desc;
    private String marker_pic;

    private List<Address> addresses;

    private boolean showAddress;
    private Uri imguri=null;
    private StorageReference mStorageRef;

    private String imgUrl;
    private addMarkerActivity.AddressResultReceiver resultReceiver;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_info);


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

        marker_id= intent.getStringExtra("Id");
        marker_name= intent.getStringExtra("Name");
        marker_desc= intent.getStringExtra("Description");
        marker_pic= intent.getStringExtra("imgUrl");

        textAddress2 = (TextView) findViewById(R.id.textAedAdr2);
        textDescr2 = (EditText) findViewById(R.id.textAedDescr2);
        textName2 = (EditText) findViewById(R.id.textAedName2);
        progressBar8 = (ProgressBar) findViewById(R.id.progressBar8);
        editAed = (Button) findViewById(R.id.btnEditAed);
        imgAdd2 = (ImageView) findViewById(R.id.imgAdd2);

        textDescr2.setText(marker_desc);
        textName2.setText(marker_name);

        Log.d(TAG,marker_pic);

        if(!marker_pic.equals("")){
            new DownloadImageTask(imgAdd2)
                    .execute(marker_pic);
        }


        //Listeners

        imgAdd2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChooser();
            }
        });

        editAed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar8.setVisibility(View.VISIBLE);

                //Upload image if there is one
                FileUploader();

            }
        });



    }

    //Put in Cloud Firestore
    public void editCollection(){

        //Fetch texts
        String AEDName = textName2.getText().toString();
        String AEDDescr = textDescr2.getText().toString();

        if(AEDName =="" || AEDDescr==""){
            Toast.makeText(MarkerInfoActivity.this, "Enter Name and Description", Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint geoloc = new GeoPoint(lat, lon);

        //Save to Database
        Map<String, Object> aed = new HashMap<>();
        aed.put("Description",AEDDescr);
        aed.put("Geolocation",geoloc);
        aed.put("ImageUrl",imgUrl);
        aed.put("Name",AEDName);


        Log.d(TAG,"Image Url after putting it in the database"+imgUrl);

        db.collection("AEDMap").document(marker_id).set(aed).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar8.setVisibility(View.GONE);
                    startActivity(new Intent(MarkerInfoActivity.this, AEDMapActivity.class));
                }
            })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        progressBar8.setVisibility(View.GONE);
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
    private void FileUploader() {

        if (imguri != null) {


            StorageReference ref = mStorageRef.child(System.currentTimeMillis() + "." + getExtension(imguri));

            ref.putFile(imguri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                            downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    if (uri.toString() != null) {
                                        imgUrl = uri.toString();
                                    }
                                    Log.d(TAG, "Image Upload Success, uri.toString: " + uri.toString());
                                    Log.d(TAG, "Image Upload Success, imgUrl: " + imgUrl);

                                    editCollection();
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
                            Log.d(TAG, "Image Failed to Upload");
                        }
                    });
        }
        else{

            imgUrl = "";
            editCollection();
        }
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
            imgAdd2.setImageURI(imguri);

        }
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            //Log.d(TAG,e.getMessage());
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (Exception e) {
            Log.d(TAG,e.getMessage());
            return null;
        }
    }

    //Initialize Map
    public void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);

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

            textAddress2.setText(title + "\n" + country + ", " + postalCode);

        }
        else{
            mMarker.setTitle("Lat: "+lat+", Lon: "+lon);
        }
    }


    //Geocoder Location Address
    public void getAddress(double lat, double lon){
        String errorMessage = "";
        addresses=null;

        Geocoder geocoder = new Geocoder(MarkerInfoActivity.this, Locale.getDefault());
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



}
