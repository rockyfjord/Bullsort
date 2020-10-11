package com.example.bullsort;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /** Called when the user taps the Capture button */
    public void gotoActivity(View view) {
        Intent intent = new Intent(this, ActivityCapture.class);
        startActivity(intent);

    }



}