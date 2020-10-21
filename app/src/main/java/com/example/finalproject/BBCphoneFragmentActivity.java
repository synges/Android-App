package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * BBCphoneFragmentActivity class creates the fragment activity which is called when the user
 * clicks on an article in the list view. Shows all relevant article details
 */
public class BBCphoneFragmentActivity extends AppCompatActivity {

    /**
     * Creates the article details fragment. Passes the data to the fragment and displays
     * all relevant article data
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbcphone);

        Bundle dataTopass = getIntent().getExtras();

        ArticleDetailsFragment dFragment = new ArticleDetailsFragment();
        dFragment.setArguments(dataTopass);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.BBCphone_fragment, dFragment)
                .commit();
    }
}
