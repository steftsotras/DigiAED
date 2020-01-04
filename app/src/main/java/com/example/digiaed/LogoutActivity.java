package com.example.digiaed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class LogoutActivity extends AppCompatActivity {

    private Button out_btn;
    private TextView txt_profil;
    private ProgressBar progressbar4;

    private FirebaseAuth mAuth;

    private static final String TAG = LogoutActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        mAuth = FirebaseAuth.getInstance();

        out_btn = (Button) findViewById(R.id.out_btn);
        progressbar4 = (ProgressBar) findViewById(R.id.progressBar4);
        txt_profil = (TextView) findViewById(R.id.txt_profilname);


        Log.d(TAG,mAuth.getCurrentUser().toString());

        if(mAuth.getCurrentUser() == null){
            Log.d(TAG,"no user");
            startActivity(new Intent(LogoutActivity.this, LoginActivity.class));
        }
        else{

            Log.d(TAG,mAuth.getCurrentUser().getEmail());
            txt_profil.setText(mAuth.getCurrentUser().getEmail());

        }



        out_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressbar4.setVisibility(View.VISIBLE);
                mAuth.signOut();

                startActivity(new Intent(LogoutActivity.this, LoginActivity.class));

                progressbar4.setVisibility(View.GONE);
            }
        });



    }



}
