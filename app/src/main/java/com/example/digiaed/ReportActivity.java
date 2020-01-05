package com.example.digiaed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class ReportActivity extends AppCompatActivity {

    private static final String TAG = ReportActivity.class.getName();

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private EditText txt_comment;
    private Button btn_report;

    private Intent intent;
    private String marker_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        intent = getIntent();
        marker_id = intent.getStringExtra("Id");

        radioGroup = (RadioGroup) findViewById(R.id.radio);
        txt_comment = (EditText) findViewById(R.id.textComment);
        btn_report = (Button) findViewById(R.id.btnReport2);

        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int radioChecked = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(radioChecked);

                String com = txt_comment.getText().toString();
                String choice = radioButton.getText().toString();

                Log.d(TAG,"marker: "+marker_id+"coment: "+com+" choice: "+choice);

                
            }
        });
    }
}
