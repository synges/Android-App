package com.example.finalproject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.nasa_img_activity.NASAImgActivity;
import com.google.android.material.navigation.NavigationView;

/**
 * Main Activity class for Nasa earth imagery
 *
 * @author Ahmed Aziz
 */
public class NASAdbActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    private EditText lon,lat;

    /**
     * on create method to fill the activity with the layout specified
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nasadb);

        lon = (EditText)findViewById(R.id.inputLon); //loads the edit text and button
        lat = (EditText)findViewById(R.id.inputLat);
        Button submitButton = (Button)findViewById(R.id.buttonSubmit);
        Button favouritesButton = (Button)findViewById(R.id.buttonFavourites);

        //This gets the toolbar from the layout:
        Toolbar tBar = (Toolbar)findViewById(R.id.toolbar);

        //This loads the toolbar, which calls onCreateOptionsMenu below:
        setSupportActionBar(tBar);

        //load and sync the drawer with the toolbar
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //load the navigation drawer
        NavigationView navigationView = findViewById(R.id.nav_view);

        //since we are using the same drawer for all activities I hid my activity from the list
        navigationView.getMenu().findItem(R.id.nasadb).setVisible(false);

        //since we are only using one drawer header view, this code is to set the info for each individual activity
        TextView info = navigationView.getHeaderView(0).findViewById(R.id.activityName);
        info.setText(R.string.nasa_earth);
        info = navigationView.getHeaderView(0).findViewById(R.id.author);
        info.setText(getString(R.string.author) + " Ahmed Aziz");
        info = navigationView.getHeaderView(0).findViewById(R.id.version);
        info.setText("Version: 1");

        //to make the icons colors visible in the drawer
        navigationView.setItemIconTintList(null);

        navigationView.setNavigationItemSelectedListener(this);

        //loading any longitude or latitude data saved in a shared pref
        SharedPreferences prefs = getSharedPreferences("coordinates", Context.MODE_PRIVATE);
        lon.setText(prefs.getString(LONGITUDE, ""));
        lat.setText(prefs.getString(LATITUDE, ""));

        /**
         *clicking the submit button loads the fragment to display the image and the info
         */
        submitButton.setOnClickListener(clk -> {

            float longitude =0;
            float latitude=0;

            Bundle dataToPass = new Bundle();

            try {
                //get the strings from the text fields for longitude and latitude and convert them to floats
                longitude = Float.parseFloat(lon.getText().toString());
                latitude = Float.parseFloat(lat.getText().toString());

                //check if the floats are within the valid range specified
                if(longitude>180 || longitude<-180 || latitude>90 || latitude<-90){
                    throw new NumberFormatException();
                }

                //put the 2 floats in the bundle to pass to the fragment
                dataToPass.putFloat(LONGITUDE, longitude);
                dataToPass.putFloat(LATITUDE, latitude);

                //load the fragment onto the screen
                NasadbFragment submitFragment = new NasadbFragment(); //create a new fragment
                submitFragment.setArguments( dataToPass ); //pass it a bundle for information
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentLocation, submitFragment) //Add the fragment in FrameLayout
                        .commit(); //actually load the fragment.

                //used to hide the keyboard on button click
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

            } catch (NumberFormatException e){
                //gets displayed to the user if he enters an invalid longitude or latitude
                Toast.makeText(NASAdbActivity.this, R.string.valid_coordinates, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }



        });

        //setting the onclick litener for the view favorites button
        favouritesButton.setOnClickListener(clk -> {
            Intent nextActivity = new Intent(NASAdbActivity.this, NasadbListActivity.class);
            startActivity(nextActivity);
        });
    }

    /**
     * save the values in the shared preferences file when the activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = getSharedPreferences("coordinates", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LONGITUDE, lon.getText().toString());
        editor.putString(LATITUDE, lat.getText().toString());
        editor.commit();
    }

    /**
     * inflate the toolbar layout
     * @param menu the menu items to be displayed
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        //since we are using the same toolbar layout I hid the button for my own activity
        menu.findItem(R.id.nasadb).setVisible(false);
        return true;
    }

    /**
     * the method is called when one of the options is selected on the toolbar
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent nextActivity=null;
        //Look at your menu XML file. Put a case for every id in that file:
        switch(item.getItemId())
        {
            //what to do when the menu item is selected:
            case R.id.home:
                finish();
                break;
            case R.id.nasa:
                nextActivity = new Intent(NASAdbActivity.this, NASAImgActivity.class);
                break;
            case R.id.bbc:
                nextActivity = new Intent(NASAdbActivity.this, BBCActivity.class);
                break;
            case R.id.guardian:
                nextActivity = new Intent(NASAdbActivity.this, GuardianActivity.class);
                break;
            case R.id.help_item:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.help)
                        .setMessage(R.string.lon_lat_instructions)
                        .setPositiveButton(R.string.ok, (click, arg) -> { })
                        .setIcon(R.drawable.help)
                        .create()
                        .show();
                break;
        }
        //if on eof the buttons that forwards you to an activity is clicked then go to the next activity and finish this one
        if(nextActivity != null){
            startActivity(nextActivity);
            finish();
        }

        return true;
    }

    /**
     * the method is called when one of the navigation menu items is clicked
     *
     * @param item the item that got clicked
     * @return
     */
    @Override
    public boolean onNavigationItemSelected( MenuItem item) {

        Intent nextActivity=null;
        //Look at your menu XML file. Put a case for every id in that file:
        switch(item.getItemId())
        {
            //what to do when the menu item is selected:
            case R.id.home:
                this.finish();
                break;
            case R.id.nasa:
                nextActivity = new Intent(NASAdbActivity.this, NASAImgActivity.class);
                break;
            case R.id.bbc:
                nextActivity = new Intent(NASAdbActivity.this, BBCActivity.class);
                break;
            case R.id.guardian:
                nextActivity = new Intent(NASAdbActivity.this, GuardianActivity.class);
                break;
            case R.id.help_item:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.help)
                        .setMessage(R.string.lon_lat_instructions)
                        .setPositiveButton(R.string.ok, (click, arg) -> { })
                        .setIcon(R.drawable.help)
                        .create()
                        .show();
                break;
        }

        if(nextActivity != null){
            startActivity(nextActivity);
            finish();
        }

        //used to close the drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }

}
