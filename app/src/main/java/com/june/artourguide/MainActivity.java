package com.june.artourguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.ar.core.ArCoreApk;

public class MainActivity extends AppCompatActivity {
    private Button btnTestAR;
    private Button btnOpenMap;
    private Button btnViewTour;
    private Button btnSignUp;
    // Set to true ensures requestInstall() triggers installation if necessary.
    private boolean mUserRequestedInstall = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTestAR = findViewById(R.id.btnTestAR);
        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnViewTour = findViewById(R.id.btnViewTour);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnOpenMap.setOnClickListener(openMapListener);
        btnViewTour.setOnClickListener(viewTourListener);
        btnSignUp.setOnClickListener(signUpListener);

        maybeEnableArButton();

    }

    void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    maybeEnableArButton();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            btnTestAR.setVisibility(View.VISIBLE);
            btnTestAR.setEnabled(true);
            // indicator on the button.
        } else { // Unsupported or unknown.
            btnTestAR.setVisibility(View.INVISIBLE);
            btnTestAR.setEnabled(false);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    private View.OnClickListener openMapListener = new View.OnClickListener() {
        public void onClick(View v) {
            // do something when the button is clicked
            Intent mapsActivity = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapsActivity);
        }
    };

    private View.OnClickListener viewTourListener = new View.OnClickListener() {
        public void onClick(View v) {
            // do something when the button is clicked
            Intent tourListActivity = new Intent(MainActivity.this, TourListActivity.class);
            startActivity(tourListActivity);
        }
    };

    private View.OnClickListener signUpListener = new View.OnClickListener() {
        public void onClick(View v) {
            // do something when the button is clicked

        }
    };

}
