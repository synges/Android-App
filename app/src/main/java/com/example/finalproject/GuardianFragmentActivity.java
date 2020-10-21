package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class GuardianFragmentActivity extends AppCompatActivity {

    /**
     * Replaces the layout with the fragment layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_guardian);

        Bundle data = getIntent().getExtras();

        GuardianFragment gFragment = new GuardianFragment();
        gFragment.setArguments(data);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.guardian_fragment, gFragment)
                .commit();
    }
}
