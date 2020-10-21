package com.example.finalproject.nasa_img_activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Activity to display favourite images
 */
public class NasaImgFavouritesActivity extends AppCompatActivity {

	Intent intent;
	ArrayList<ImgData> favImgList;
	ListView listView;
	private SQLiteDatabase db;


	/**
	 * Initializes favourite list, fills list view, sets listeners
	 *
	 * @param savedInstanceState Bundle to hold state information
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nasa_img_favourites);
		intent = getIntent();
		favImgList = (ArrayList<ImgData>) intent.getExtras().get("favImageList");
		listView = findViewById(R.id.NasaImgListView);
		Button backButton = findViewById(R.id.NasaImgBackBtn);


		ListAdapter adapter = new ListAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemLongClickListener((adapt, view, position, id) -> {
			prepareDB();
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(R.string.help)
					.setTitle(((TextView) view.findViewById(R.id.TitleText)).getText())
					.setMessage(R.string.DeleteMessage1)
					.setPositiveButton(R.string.yes, (click, arg) -> {
						//delete the item from the array list
						favImgList.remove(position);
						//delete the item from the database
						TextView dateText = view.findViewById(R.id.DateText);
						String[] dateArray = dateText.getText().toString().split(" ");
						removeFromDB(dateArray[1]);
						adapter.notifyDataSetChanged();
					})
					.setNegativeButton(R.string.close, (click, arg) -> {
					})
					.create()
					.show();
			return true;
		});

		listView.setOnItemClickListener((adapterView, view, position, id) -> {
			TextView urlView = view.findViewById(R.id.urlList);
			String message = ((TextView) view.findViewById(R.id.DateText)).getText() + "\n\n\t" + ((TextView) view.findViewById(R.id.DescriptionText)).getText();
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(R.string.help)
					.setTitle(((TextView) view.findViewById(R.id.TitleText)).getText())
					.setMessage(message)
					.setPositiveButton(R.string.OpenImageInBrowser, (click, arg) -> {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlView.getText().toString()));
						startActivity(browserIntent);
					})
					.setNegativeButton(R.string.close, (click, arg) -> {
					})
					.create()
					.show();

		});

		backButton.setOnClickListener(clk -> {
			finish();
		});

	}

	/**
	 * Opens connection to database
	 */
	private void prepareDB() {
		NasaImgDBOpener dbOpener = new NasaImgDBOpener(this);
		db = dbOpener.getWritableDatabase();
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
	 * The list adapter class that is used to display the items in the listview
	 */
	private class ListAdapter extends BaseAdapter {

		/**
		 * Retrieves the number of instances in the favImgList
		 *
		 * @return Returns the amount of Images in favList
		 */
		public int getCount() {
			return favImgList.size();
		}

		/**
		 * Gets fav image from list at position of parameter position
		 *
		 * @param position position of favourite image
		 * @return returns the ImgData of the fav img requested
		 */
		public Object getItem(int position) {
			return favImgList.get(position);
		}

		/**
		 * Gets the id of a specified image based on the position of that image
		 *
		 * @param position position of the image
		 * @return the id of the image
		 */
		public long getItemId(int position) {
			return favImgList.get(position).getId();
		}

		/**
		 * Inflates a view to be put into listView
		 *
		 * @param position position of the image in image list
		 * @param old      old view to be replaced (not used)
		 * @param parent   parent viewgroup, used in inflater
		 * @return A new view to be placed into the ListView
		 */
		public View getView(int position, View old, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();

			View newView = inflater.inflate(R.layout.list_view_nasaimg, parent, false);

			TextView title = newView.findViewById(R.id.TitleText);
			title.setText(getString(R.string.title) + " " + favImgList.get(position).getImageTitle());
			title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

			TextView date = newView.findViewById(R.id.DateText);
			date.setText(getString(R.string.date) + " " + favImgList.get(position).getDate());

			TextView desc = newView.findViewById(R.id.DescriptionText);
			desc.setText(favImgList.get(position).getDescription());
			TextView url = newView.findViewById(R.id.urlList);
			url.setText(favImgList.get(position).getImageUrl());
			ImageView imageView = newView.findViewById(R.id.NasaImgListImage);
			FileInputStream fis = null;
			try {
				fis = openFileInput(favImgList.get(position).getDate());
			} catch (FileNotFoundException e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			Bitmap image = BitmapFactory.decodeStream(fis);
			imageView.setImageBitmap(image);

			//return it to be put in the table
			return newView;
		}
	}

}
