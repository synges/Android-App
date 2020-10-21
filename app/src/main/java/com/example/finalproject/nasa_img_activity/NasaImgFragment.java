package com.example.finalproject.nasa_img_activity;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NasaImgFragment extends Fragment {


	private TextView titleTextView;
	private TextView dateTextView;
	private Button urlButton;
	private String imgDescription;
	private ProgressBar progressBar;
	private Button addToFavButton;
	private SQLiteDatabase db;


	/**
	 * Empty constructor needed in NasaImgActivity
	 */
	public NasaImgFragment() {
		// Required empty public constructor
	}


	/**
	 * Initializes views, loads database, and sets fragment listeners
	 *
	 * @param inflater           used to inflate a view to hold fragment layout
	 * @param container          ViewGroup used by inflater
	 * @param savedInstanceState Bundle to hold saved state information
	 * @return
	 */


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		Bundle data = getArguments();
		parseImageData(data);
		View view = inflater.inflate(R.layout.fragment_nasa_img, container, false);
		titleTextView = view.findViewById(R.id.NasaImgFragmentImageTitle);
		dateTextView = view.findViewById(R.id.NasaImgFragmentImageDate);
		urlButton = view.findViewById(R.id.NasaImgFragmentImageUrl);
		progressBar = view.findViewById(R.id.NasaImgProgressBar);
		addToFavButton = view.findViewById(R.id.NasaImgAddToFav);
		prepareDB();
//  When url button is pressed open a browser to view the image
		urlButton.setOnClickListener(v -> {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlButton.getText().toString()));
			startActivity(browserIntent);
		});
//		When add to fav is pushed add the image to the database, create a snackbar to remove it from favourites
		addToFavButton.setOnClickListener(v -> {
			String Query = "Select * from " + NasaImgDBOpener.TABLE_NAME + " where " + NasaImgDBOpener.COL_DATE + " = '" + dateTextView.getText().toString() + "'";
			Cursor cursor = db.rawQuery(Query, null);
			if (cursor.getCount() <= 0) {
				cursor.close();
				addToDB(titleTextView.getText().toString(), dateTextView.getText().toString(), imgDescription, urlButton.getText().toString());
				Snackbar snackbar = Snackbar.make(this.getView(), R.string.NasaImgAddToDBSnack, BaseTransientBottomBar.LENGTH_SHORT);
				snackbar.setAction(R.string.undo, new MyUndoListener());
				snackbar.show();
			} else
				Toast.makeText(NasaImgFragment.this.getContext(), R.string.AlreadyFav, Toast.LENGTH_LONG).show();


		});

		return view;

	}


	/**
	 * Converts image data into integers to parse the json data from the api
	 *
	 * @param data Bundle holding date information of the image to be searched for
	 */
	private void parseImageData(Bundle data) {
		JsonParser jsonParser = new JsonParser();
		int day = data.getInt("DAY");
		int month = data.getInt("MONTH") + 1;
		int year = data.getInt("YEAR");
		jsonParser.execute("https://api.nasa.gov/planetary/apod?api_key=hYChnfOfhe4rWgTji6IkpTd3gSHcFCiAWLFE5h5S&date=" + year + "-" + month + "-" + day);


	}

	/**
	 * Opens connection to database
	 */
	private void prepareDB() {
		NasaImgDBOpener dbOpener = new NasaImgDBOpener(getActivity());
		db = dbOpener.getWritableDatabase();
	}

	/**
	 * Adds image to database based on given parameters
	 *
	 * @param title       Title of the image to be added to database
	 * @param date        Date of the image to be added to the database
	 * @param description Description of the image to be added to the database
	 * @param url         url of the image to be added to the database
	 */
	private void addToDB(String title, String date, String description, String url) {
		ContentValues newRowValues = new ContentValues();
		newRowValues.put("TITLE", title);
		newRowValues.put("DATE", date);
		newRowValues.put("IMAGE", url);
		newRowValues.put("DESCRIPTION", description);
		db.insert(NasaImgDBOpener.TABLE_NAME, null, newRowValues);

	}


	/**
	 * Removes an image from the database
	 *
	 * @param date Date of the image to be removed
	 */
	protected void removeFromDB(String date) {
		db.delete(NasaImgDBOpener.TABLE_NAME, NasaImgDBOpener.COL_DATE + "= ?", new String[]{date});
	}


	/**
	 * Parser Class to parse api information on the background thread using AsyncTask
	 */
	private class JsonParser extends AsyncTask<String, Integer, String> {
		private String imgDate;
		private String imgUrl;
		private String imgTitle;

		/**
		 * Opens connection to api, Retrives and stores Json info
		 *
		 * @param args Array of 1 argument, the api url for the specified image
		 * @return String "Done"
		 */
		public String doInBackground(String... args) {
			try {

				URL url = new URL(args[0]);

				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				publishProgress(10);
				InputStream response = urlConnection.getInputStream();
				publishProgress(20);


				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8), 8);
				StringBuilder stringBuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line + "\n");
				}
				String result = stringBuilder.toString();


				JSONObject imgData = new JSONObject(result);
				publishProgress(30);
				imgDate = imgData.getString("date");
				imgUrl = imgData.getString("url");
				publishProgress(40);
				imgTitle = imgData.getString("title");
				imgDescription = imgData.getString("explanation");
				publishProgress(50);

				File file = getActivity().getFileStreamPath(imgDate);
				if (!file.exists()) {
					HttpURLConnection connection;
					Bitmap image = null;
					URL imageUrl;
					try {
						imageUrl = new URL(imgUrl);

						connection = (HttpURLConnection) imageUrl.openConnection();
						connection.connect();
						publishProgress(60);

						int responseCode = connection.getResponseCode();
						if (responseCode == 200) {
							image = BitmapFactory.decodeStream(connection.getInputStream());
							publishProgress(70);

						}
						FileOutputStream outputStream = getActivity().openFileOutput(imgDate, Context.MODE_PRIVATE);
						image.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
						publishProgress(85);

						outputStream.flush();
						outputStream.close();
						publishProgress(100);


						Log.i("Image download", "Image downloaded from website");
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}


			} catch (Exception e) {
				imgDate = "No image for this date";
				imgDescription = "";
				imgTitle = "No Image Found";
				imgUrl = "No Image Found";


				Log.e("Error", e.getMessage());
			}

			return "Done";
		}


		/**
		 * Makes the progress bar visible and updates the progress of the bar
		 *
		 * @param values The value of the progress bar should be set to
		 */
		public void onProgressUpdate(Integer... values) {

			progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(values[0]);

		}

		/**
		 * When the background thread is done, make the progress bar invisible, and set the fragment views' text to match the info retrieved
		 *
		 * @param fromDoInBackground
		 */
		public void onPostExecute(String fromDoInBackground) {
			progressBar.setVisibility(View.INVISIBLE);
			dateTextView.setText(imgDate);
			titleTextView.setText(imgTitle);
			urlButton.setText(imgUrl);
		}
	}

	/**
	 * Undo listener for the add to database snackbar
	 */
	private class MyUndoListener implements View.OnClickListener {
		/**
		 * When the user clicks undo, remove the item from the db and create a redo snackbar
		 *
		 * @param v view that holds the listener
		 */
		@Override
		public void onClick(View v) {
			removeFromDB(dateTextView.getText().toString());
			Snackbar snackbar = Snackbar.make(NasaImgFragment.this.getView(), R.string.NasaImgRemoveFromDBSnack, BaseTransientBottomBar.LENGTH_SHORT);
			snackbar.setAction(R.string.undo, new MyRedoListener());
			snackbar.show();
		}

	}

	/**
	 * Undo listener for the remove from database snackbar
	 */
	private class MyRedoListener implements View.OnClickListener {
		/**
		 * When the user clicks undo, re-add the item to the db and create another undo snackbar
		 *
		 * @param v view that holds the listener
		 */
		@Override
		public void onClick(View v) {
			addToDB(titleTextView.getText().toString(), dateTextView.getText().toString(), imgDescription, urlButton.getText().toString());
			Snackbar snackbar = Snackbar.make(NasaImgFragment.this.getView(), R.string.NasaImgAddToDBSnack, BaseTransientBottomBar.LENGTH_SHORT);
			snackbar.setAction(R.string.undo, new MyUndoListener());
			snackbar.show();
		}

	}


}
