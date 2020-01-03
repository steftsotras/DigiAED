package com.example.digiaed;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MarkerInfoActivity extends AppCompatActivity {


    private EditText textName2;
    private EditText textDescr2;
    private ImageView ic_pic;
    private ProgressBar progressBar8;
    private TextView textAddress2;
    private Button editAed;
    private ImageView imgAdd2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_info);


        textAddress2 = (TextView) findViewById(R.id.textAedAdr2);
        textDescr2 = (EditText) findViewById(R.id.textAedDescr2);
        textName2 = (EditText) findViewById(R.id.textAedName2);
        progressBar8 = (ProgressBar) findViewById(R.id.progressBar8);
        editAed = (Button) findViewById(R.id.btnEditAed);
        imgAdd2 = (ImageView) findViewById(R.id.imgAdd2);




    }
}
