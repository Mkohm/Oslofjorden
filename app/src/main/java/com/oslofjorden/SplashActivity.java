package com.oslofjorden;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "onCreate: starter splash");
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();

    }
}