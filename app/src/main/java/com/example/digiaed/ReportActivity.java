package com.example.digiaed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private static final String TAG = ReportActivity.class.getName();

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private EditText txt_comment;
    private Button btn_report;
    private ProgressBar progressBar9;

    private Intent intent;
    private String marker_id;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = FirebaseFirestore.getInstance();

        intent = getIntent();
        marker_id = intent.getStringExtra("Id");

        radioGroup = (RadioGroup) findViewById(R.id.radio);
        txt_comment = (EditText) findViewById(R.id.textComment);
        btn_report = (Button) findViewById(R.id.btnReport2);
        progressBar9 = (ProgressBar) findViewById(R.id.progressBar9);

        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar9.setVisibility(View.VISIBLE);

                int radioChecked = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(radioChecked);

                String com = txt_comment.getText().toString();
                String choice = radioButton.getText().toString();

                Log.d(TAG,"marker: "+marker_id+"coment: "+com+" choice: "+choice);


                Map<String, Object> aedreport = new HashMap<>();
                aedreport.put("AEDMapID",marker_id);
                aedreport.put("Comment",com);
                aedreport.put("Type",choice);


                db.collection("AEDReport").add(aedreport).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                        progressBar9.setVisibility(View.GONE);
                        startActivity(new Intent(ReportActivity.this, AEDMapActivity.class));
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                                progressBar9.setVisibility(View.GONE);
                            }
                        });

            }
        });
    }
}
