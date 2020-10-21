package com.example.finalproject.nasa_img_activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.finalproject.BBCActivity;
import com.example.finalproject.GuardianActivity;
import com.example.finalproject.NASAdbActivity;
import com.example.finalproject.R;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Noah Cheesman (manoverboa2)
 * @version "%I%, %G%"
 */
public class NASAImgActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	protected ArrayList<ImgData> favImageList;

	private SQLiteDatabase db;
	private Calendar imageDate = null;


	/**
	 * initializes views, loads toolbar and menu, and main page listeners
	 *
	 * @param savedInstanceState Bundle to hold state information
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nasaimg);

		Button dateButton = findViewById(R.id.NasaImgDatePickerBtn);
		Button submitButton = findViewById(R.id.NasaImgSubmitBtn);
		Button favButton = findViewById(R.id.NasaImgFavBtn);
		TextView dateText = findViewById(R.id.NasaImgDateText);
		NavigationView navigationView = findViewById(R.id.nav_view);
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		favImageList = new ArrayList<>();
		Toolbar tBar = findViewById(R.id.toolbar);
		Bundle bundle = new Bundle();


		//Loads the toolbar, then calls onCreateOptionsMenu below:
		setSupportActionBar(tBar);

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tBar, R.string.open, R.string.close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		//hides this activity from the list
		navigationView.getMenu().findItem(R.id.nasa).setVisible(false);

		//Sets the header info for this activity
		TextView info = navigationView.getHeaderView(0).findViewById(R.id.activityName);
		info.setText(R.string.nasa_image);
		info = navigationView.getHeaderView(0).findViewById(R.id.author);
		info.setText(getString(R.string.author) + " Noah Cheesman");
		info = navigationView.getHeaderView(0).findViewById(R.id.version);
		info.setText(getString(R.string.versionNum));

		//to make the icons colors visible in the drawer
		navigationView.setItemIconTintList(null);

		navigationView.setNavigationItemSelectedListener(this);

		SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.nasaImgKey), MODE_PRIVATE);
		SharedPreferences.Editor editPrefs = sharedPreferences.edit();
		dateText.setText(sharedPreferences.getString("Date", ""));

		//When the date is set change the date field to match
		DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month, dayOfMonth) -> {
			String dateString = year + "/" + (month + 1) + "/" + dayOfMonth;
			dateText.setText(dateString);
			setImageDate(year, month, dayOfMonth);
		};

		//Create a dialog to set date using a DatePickerDialog
		dateButton.setOnClickListener(v -> {
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyAppTheme, onDateSetListener, year, month, day);
			datePickerDialog.getDatePicker().setMaxDate(cal.getTimeInMillis());
			datePickerDialog.show();
		});
		//When the user presses search get the date from the date text box and call fragment. If date is wrong format, create a toast telling user
		submitButton.setOnClickListener(v -> {
			try {
				String[] splitDate = dateText.getText().toString().split("/");
				if (splitDate.length != 3)
					throw new NullPointerException();
				setImageDate(Integer.parseInt(splitDate[0]), (Integer.parseInt(splitDate[1]) - 1), Integer.parseInt(splitDate[2]));
				Calendar currentCal = Calendar.getInstance();
				if (currentCal.before(imageDate))
					throw new NumberFormatException("Date is before current Date");
				bundle.putInt("DAY", imageDate.get(Calendar.DAY_OF_MONTH));
				bundle.putInt("MONTH", imageDate.get(Calendar.MONTH));
				bundle.putInt("YEAR", imageDate.get(Calendar.YEAR));
				NasaImgFragment fragment = new NasaImgFragment();
				fragment.setArguments(bundle);
				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.NasaImgFragmentHolder, fragment) //Add the fragment in FrameLayout
						.commit(); //actually load the fragment.
				editPrefs.putString("Date", dateText.getText().toString());
				editPrefs.commit();
			} catch (NumberFormatException e) {
				Toast.makeText(NASAImgActivity.this, R.string.FutureDate, Toast.LENGTH_LONG).show();
			} catch (NullPointerException e) {
				Toast.makeText(NASAImgActivity.this, R.string.SelectADate, Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				if (e.getMessage() == null)
					Log.e(e.getClass().toString(), "---No Message---");
				else
					Log.e(e.getClass().toString(), e.getMessage());
			}
		});
		//Switch activity to favourites activity to view a list of all favourites
		favButton.setOnClickListener(v -> {
			Intent nextActivity = new Intent(NASAImgActivity.this, NasaImgFavouritesActivity.class);
			favImageList.clear();
			loadDataFromDatabase();
//Checks if a file exists in favourites list, file belongs to the last searched image, or if file does not belong to this activity, if not deletes file
			File root = new File(this.getFilesDir().getAbsolutePath());
			File[] Files = root.listFiles();
			Boolean delete;
			Fragment currentFragment = getSupportFragmentManager()
					.findFragmentById(R.id.NasaImgFragmentHolder);
			if (Files != null) {
				for (int i = 0; i < Files.length; i++) {
					delete = true;
					String fileName = Files[i].getName();
					for (int j = 0; j < favImageList.size(); j++) {
						String dbItemDate = favImageList.get(j).getDate();
						String[] fileNameSplit = fileName.split("-");
						if (fileName.equals(dbItemDate)) {
							delete = false;
							j = favImageList.size();
						} else if (fileNameSplit.length != 3 || (getSupportFragmentManager().findFragmentById(R.id.NasaImgFragmentHolder) != null && fileName.equals(((TextView) currentFragment.getActivity().findViewById(R.id.NasaImgFragmentImageDate)).getText().toString())))
							delete = false;

					}
					if (delete) {
						System.out.println(Files[i].getAbsolutePath());
						System.out.println(Files[i].delete());
					}

				}
			}
			nextActivity.putExtra("favImageList", favImageList);
			startActivityForResult(nextActivity, 200);
		});

	}

	/**
	 * releases all remaining resources and deletes all images that aren't favourites
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		favImageList.clear();
		loadDataFromDatabase();

		File root = new File(this.getFilesDir().getAbsolutePath());
		File[] Files = root.listFiles();
		Boolean delete;
		if (Files != null) {
			for (int i = 0; i < Files.length; i++) {
				delete = true;
				String fileName = Files[i].getName();
				for (int j = 0; j < favImageList.size(); j++) {
					String dbItemDate = favImageList.get(j).getDate();
					if (fileName.equals(dbItemDate)) {
						delete = false;
						j = favImageList.size();
					}

				}
				if (delete) {
					System.out.println(Files[i].getAbsolutePath());
					System.out.println(Files[i].delete());
				}

			}
		}

		db.close();

	}

	/**
	 * Creates ImageData objects to hold the data in the database, then stores the objects in the images array
	 *
	 * @see ImgData
	 * @see NasaImgDBOpener
	 */
	private void loadDataFromDatabase() {
		NasaImgDBOpener dbOpener = new NasaImgDBOpener(this);
		db = dbOpener.getWritableDatabase();


		String[] columns = {NasaImgDBOpener.COL_ID, NasaImgDBOpener.COL_TITLE, NasaImgDBOpener.COL_DATE, NasaImgDBOpener.COL_IMAGE, NasaImgDBOpener.COL_DESCRIPTION};
		Cursor results = db.query(false, NasaImgDBOpener.TABLE_NAME, columns, null, null, null, null, null, null);
		printCursor(results, db.getVersion());
		int imageColumnIndex = results.getColumnIndex(NasaImgDBOpener.COL_IMAGE);
		int descriptionColIndex = results.getColumnIndex(NasaImgDBOpener.COL_DESCRIPTION);
		int idColIndex = results.getColumnIndex(NasaImgDBOpener.COL_ID);
		int titleColIndex = results.getColumnIndex(NasaImgDBOpener.COL_TITLE);
		int dateColIndex = results.getColumnIndex(NasaImgDBOpener.COL_DATE);
		if (!favImageList.isEmpty())
			favImageList.clear();
		if (results != null && results.getCount() > 0) {
			do {
				String description = results.getString(descriptionColIndex);
				String image = results.getString(imageColumnIndex);
				String title = results.getString(titleColIndex);
				String date = results.getString(dateColIndex);
				long id = results.getLong(idColIndex);

				favImageList.add(new ImgData(id, title, image, description, date));

			} while (results.moveToNext());
		}
		results.close();
	}

	/**
	 * Outputs all information stored in cursor to information log.
	 *
	 * @param c       Cursor which information will be retrieved from
	 * @param version Database version number
	 */
	private void printCursor(Cursor c, int version) {
		c.moveToFirst();
		Log.i("Info DB Version Num: ", Integer.toString(version));
		Log.i("Info Num of columns ", Integer.toString(c.getColumnCount()));
		for (int i = 0; i < c.getColumnNames().length; i++) {
			Log.i("Info Column Names ", c.getColumnNames()[i]);
		}
		Log.i("Info Num of Results ", Integer.toString(c.getCount()));
		int counter = 1;
		while (c.moveToNext()) {
			Log.i("\nInfo Result " + counter + ":", "\nId: " + c.getInt(c.getColumnIndex(NasaImgDBOpener.COL_ID)) +
					"\nInfo Title: " + c.getString(c.getColumnIndex(NasaImgDBOpener.COL_TITLE)) +
					"\nInfo Date: " + c.getString(c.getColumnIndex(NasaImgDBOpener.COL_DATE)) +
					"\nInfo Description: " + c.getString(c.getColumnIndex(NasaImgDBOpener.COL_DESCRIPTION)) +
					"\nInfo ImageURL: " + c.getString(c.getColumnIndex(NasaImgDBOpener.COL_IMAGE)));
			counter++;
		}
		c.moveToFirst();
	}

	/**
	 * Sets calendar objects date from parameters
	 *
	 * @param year  The year to be set
	 * @param month The month to be set (The year starts at 0 and ends at 11)
	 * @param day   The day of the month to be set
	 */
	private void setImageDate(int year, int month, int day) {
		imageDate = Calendar.getInstance();
		imageDate.set(Calendar.YEAR, year);
		imageDate.set(Calendar.MONTH, month);
		imageDate.set(Calendar.DAY_OF_MONTH, day);
	}


	/**
	 * inflate the toolbar layout
	 *
	 * @param menu the menu items to be displayed
	 * @return true
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_toolbar, menu);
		//since we are using the same toolbar layout I hid the button for my own activity
		menu.findItem(R.id.nasa).setVisible(false);
		return true;
	}

	/**
	 * the method is called when one of the options is selected on the toolbar
	 *
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent nextActivity = null;
		//Look at your menu XML file. Put a case for every id in that file:
		switch (item.getItemId()) {
			//what to do when the menu item is selected:
			case R.id.home:
				finish();
				break;
			case R.id.nasadb:
				nextActivity = new Intent(NASAImgActivity.this, NASAdbActivity.class);
				break;
			case R.id.bbc:
				nextActivity = new Intent(NASAImgActivity.this, BBCActivity.class);
				break;
			case R.id.guardian:
				nextActivity = new Intent(NASAImgActivity.this, GuardianActivity.class);
				break;
			case R.id.help_item:
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle(R.string.help)
						.setMessage(R.string.img_of_day_instructions)
						.setPositiveButton(R.string.ok, (click, arg) -> {
						})
						.setIcon(R.drawable.help)
						.create()
						.show();
				break;
		}
		//if on eof the buttons that forwards you to an activity is clicked then go to the next activity and finish this one
		if (nextActivity != null) {
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
	public boolean onNavigationItemSelected(MenuItem item) {

		Intent nextActivity = null;
		//Look at your menu XML file. Put a case for every id in that file:
		switch (item.getItemId()) {
			//what to do when the menu item is selected:
			case R.id.home:
				this.finish();
				break;
			case R.id.nasadb:
				nextActivity = new Intent(NASAImgActivity.this, NASAdbActivity.class);
				break;
			case R.id.bbc:
				nextActivity = new Intent(NASAImgActivity.this, BBCActivity.class);
				break;
			case R.id.guardian:
				nextActivity = new Intent(NASAImgActivity.this, GuardianActivity.class);
				break;
			case R.id.help_item:
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle(R.string.help)
						.setMessage(R.string.img_of_day_instructions)
						.setPositiveButton(R.string.ok, (click, arg) -> {
						})
						.setIcon(R.drawable.help)
						.create()
						.show();
				break;
		}

		if (nextActivity != null) {
			startActivity(nextActivity);
			finish();
		}

		//used to close the drawer
		DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
		drawerLayout.closeDrawer(GravityCompat.START);

		return false;
	}


}
