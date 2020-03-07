package com.june.artourguide;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main Activity for the Cloud Anchors Codelab.
 *
 * <p>The bulk of the logic resides in the {@link CloudAnchorFragment}. The activity only creates
 * the fragment and attaches it to this Activity.
 */
public class ARActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.fragment_container);
        if (frag == null) {
            frag = new CloudAnchorFragment();
            fm.beginTransaction().add(R.id.fragment_container, frag).commit();
        }
    }
}
