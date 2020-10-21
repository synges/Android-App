package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.finalproject.nasa_img_activity.NASAImgActivity;
import com.google.android.material.navigation.NavigationView;

/**
 * Main Activity class for the application
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	/**
	 * The OnCreate method gets exuceted when the activity is created and ready to be loaded on the screen
	 *
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//loading all the buttons from the layout
		Button btnGuardian = findViewById(R.id.btn_Guardian);
		Button btnNASAimg = findViewById(R.id.btn_NASAimg);
		Button btnNASAdb = findViewById(R.id.btn_NASAdb);
		Button btnBBC = findViewById(R.id.btn_BBC);

		//This gets the toolbar from the layout:
		Toolbar tBar = findViewById(R.id.toolbar);

		//This loads the toolbar, which calls onCreateOptionsMenu below:
		setSupportActionBar(tBar);

		//to get the drawer to sync with the toolbar
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tBar, R.string.open, R.string.close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		//load the navigation drawer
		NavigationView navigationView = findViewById(R.id.nav_view);

		//since we are using the same drawer for all activities I hid my activity from the list
		navigationView.getMenu().findItem(R.id.home).setVisible(false);

		//since we are only using one drawer header view this code is to set the info
		TextView info = navigationView.getHeaderView(0).findViewById(R.id.activityName);
		info.setText(R.string.final_project);
		info = navigationView.getHeaderView(0).findViewById(R.id.author);
		info.setText(getString(R.string.author) + "\nAhmed Aziz\nMohammed El Sherif\nMax Besner\nNoah Cheesman");
		info = navigationView.getHeaderView(0).findViewById(R.id.version);
		info.setText("Version: 1.0");

		//to make the icons colors visible in the drawer
		navigationView.setItemIconTintList(null);
		//to set the select listener for the navigation bar
		navigationView.setNavigationItemSelectedListener(this);

		//setting the on click listeners for all the buttons
		btnGuardian.setOnClickListener(btn -> {
			Intent goToGuardian = new Intent(MainActivity.this, GuardianActivity.class);
			startActivity(goToGuardian);
		});

		btnNASAimg.setOnClickListener(btn -> {
			Intent goToNASAimg = new Intent(MainActivity.this, NASAImgActivity.class);
			startActivity(goToNASAimg);
		});

		btnNASAdb.setOnClickListener(btn -> {
			Intent goToNASAdb = new Intent(MainActivity.this, NASAdbActivity.class);
			startActivity(goToNASAdb);
		});

		btnBBC.setOnClickListener(btn -> {
			Intent goToBBC = new Intent(MainActivity.this, BBCActivity.class);
			startActivity(goToBBC);
		});


	}

	/**
	 * This methods gets called when we set the toolbar
	 *
	 * @param menu
	 * @return boolean true
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_toolbar, menu);
		//since we are using the same toolbar layout I hid the button for my own activity
		menu.findItem(R.id.home).setVisible(false);
		return true;
	}

	/**
	 * The method get called when one of the toolbar items gets clicked
	 *
	 * @param item the item that gets clicked
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent nextActivity = null;
		//Look at your menu XML file. Put a case for every id in that file:
		switch (item.getItemId()) {
			//what to do when the menu item is selected:
			case R.id.nasadb:
				nextActivity = new Intent(MainActivity.this, NASAdbActivity.class);
				break;
			case R.id.nasa:
				nextActivity = new Intent(MainActivity.this, NASAImgActivity.class);
				break;
			case R.id.bbc:
				nextActivity = new Intent(MainActivity.this, BBCActivity.class);
				break;
			case R.id.guardian:
				nextActivity = new Intent(MainActivity.this, GuardianActivity.class);
				break;
			case R.id.help_item:
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle(R.string.help)
						.setMessage(R.string.welcome_message)
						.setPositiveButton(R.string.ok, (click, arg) -> {
						})
						.setIcon(R.drawable.help)
						.create()
						.show();
				break;
		}
		//if one of the buttons that gets you to a next activity is clicked then nextactivity isn't null
		if (nextActivity != null) {
			startActivity(nextActivity);
		}

		return true;
	}

	/**
	 * The method gets called when one of the navigation drawer items get clicked
	 *
	 * @param item the item that gor clicked
	 * @return boolean false
	 */
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {

		Intent nextActivity = null;
		//Look at your menu XML file. Put a case for every id in that file:
		switch (item.getItemId()) {
			//what to do when the menu item is selected:
			case R.id.nasadb:
				nextActivity = new Intent(MainActivity.this, NASAdbActivity.class);
				break;
			case R.id.nasa:
				nextActivity = new Intent(MainActivity.this, NASAImgActivity.class);
				break;
			case R.id.bbc:
				nextActivity = new Intent(MainActivity.this, BBCActivity.class);
				break;
			case R.id.guardian:
				nextActivity = new Intent(MainActivity.this, GuardianActivity.class);
				break;
			case R.id.help_item:
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle(R.string.help)
						.setMessage(R.string.welcome_message)
						.setPositiveButton(R.string.ok, (click, arg) -> {
						})
						.setIcon(R.drawable.help)
						.create()
						.show();
				break;
		}

		//if one of the buttons that gets you to a next activity is clicked then nextactivity isn't null
		if (nextActivity != null) {
			startActivity(nextActivity);
		}

		//used to close the drawer
		DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
		drawerLayout.closeDrawer(GravityCompat.START);

		return false;
	}
}
