package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *the class for the favorite list activity
 */
public class NasadbListActivity extends AppCompatActivity {

    private Bundle dataFromFragment;
    private ArrayList<NasaImage> list = new ArrayList<>();
    private ListAdapter listAdapter;
    private MyOpener dbOpener;
    private SQLiteDatabase db;

    /**
     * the on create method is exuected when we first load the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nasadb_list);

        dataFromFragment = getIntent().getExtras(); //get the data that was passed from Fragment
        //method to get the data from the database and put it in the arraylist
        loadDataFromDatabase();

        // if we are coming from the main activity not the fragment no data will be in the intent
        if (dataFromFragment != null) {
            float lonValue = dataFromFragment.getFloat(NASAdbActivity.LONGITUDE);
            float latValue = dataFromFragment.getFloat(NASAdbActivity.LATITUDE);
            String dateValue = dataFromFragment.getString(NasadbFragment.IMAGE_DATE);
            String nameValue = dataFromFragment.getString(NasadbFragment.IMAGE_NAME);
            saveDataToDatabase(nameValue, dateValue, lonValue, latValue);
        }


        ListView favoriteList = (ListView) findViewById(R.id.theList);
        favoriteList.setAdapter(listAdapter = new ListAdapter());

        //setting the on click listener of a long press on the item list
        favoriteList.setOnItemLongClickListener((parent, view, pos, id) -> {
            //displaying a snackbar with the option to delete the item
            Snackbar.make(view, R.string.delete_this, Snackbar.LENGTH_LONG).setAction(R.string.yes, click -> {
                deleteDataFromDatabase(pos, id);
                listAdapter.notifyDataSetChanged();
            }).show();
            return true;
        });

        // a normal press on an item displayes an alert dialog that get inflated with the layout of the fragment after the buttons are removed
        favoriteList.setOnItemClickListener((parent, view, pos, id) -> {
            View imageView = getLayoutInflater().inflate(R.layout.fragment_nasadb, parent, false);

            //hide the finish and add favourites button from the fragment layout
            ((Button)imageView.findViewById(R.id.finishButton)).setVisibility(View.GONE);
            ((Button)imageView.findViewById(R.id.addFavourites)).setVisibility(View.GONE);

            //set the text views to be gone from the layout
            ((TextView)imageView.findViewById(R.id.lon)).setVisibility(View.GONE);;
            ((TextView)imageView.findViewById(R.id.lat)).setVisibility(View.GONE);;
            ((TextView)imageView.findViewById(R.id.date)).setVisibility(View.GONE);;

            //set the image by getting it from the hardware
            FileInputStream fis = null;
            try {
                fis = openFileInput(list.get(pos).getName());
            } catch (FileNotFoundException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            Bitmap image = BitmapFactory.decodeStream(fis);
            ((ImageView)imageView.findViewById(R.id.imageNasa)).setImageBitmap(image);

            //display the alert dialog with the info at it's title and the image as it's layout, the positive button is used to delete the picture or neutal button to close the alert dialog
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.longitude)+" "+ list.get(pos).getLon() + "\t\t\t" +getString(R.string.latitude)+ " "+ list.get(pos).getLat() + "\n"+ getString(R.string.date) +" " + list.get(pos).getDate())
                    .setPositiveButton(R.string.delete_this, (click, arg) -> { deleteDataFromDatabase(pos, id); listAdapter.notifyDataSetChanged();})
                    .setNeutralButton(R.string.close, (click, arg)-> {})
                    .setView(imageView)
                    .create()
                    .show();
        });

        //the finish button returns to the previous activity
        Button finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(clk -> {
            finish();
        });

    }

    /**
     * The method is used to save an item to the database
     *
     * @param name name if the img to be saved
     * @param date the date associated with the image
     * @param lon the longitude of the image
     * @param lat the latitude of the image
     */
    private void saveDataToDatabase(String name, String date, float lon, float lat) {
        //checks if the the item to be added to the database is repeated(already exists) or not by checking the iamge name again all the images in the arraylist
        boolean isRepeated = false;
        for(NasaImage item: list){
            if(name.equals(item.getName())) {
                isRepeated = true;
                Toast.makeText(NasadbListActivity.this, R.string.item_exist, Toast.LENGTH_LONG).show();
            }
        }

        //if the item is not repeated then proceeded to add it to the database
        if(!isRepeated){
            ContentValues cv = new ContentValues();
            cv.put(dbOpener.COL_NAME, name);
            cv.put(dbOpener.COL_DATE, date);
            cv.put(dbOpener.COL_LON, lon);
            cv.put(dbOpener.COL_lAT, lat);

            long id = db.insert(dbOpener.TABLE_NAME, null, cv);
            list.add(new NasaImage(name, date, lon, lat, id));
        }
    }


    /**
     * the method is used to delete an item from the database and the array list
     * @param position the position of the item in the arraylist
     * @param id the id of the item in the database
     */
    private void deleteDataFromDatabase(int position, long id) {
        //delete the image from the hardware
        File file = getBaseContext().getFileStreamPath(list.get(position).getName());
        file.delete();
        //delete the item from the array list
        list.remove(position);
        //delete the item from the database
        db.delete(MyOpener.TABLE_NAME, MyOpener.COL_ID + "= ?", new String[]{Long.toString(id)});
    }

    /**
     * the method is used to load the data from the database and into the arraylist
     */
    private void loadDataFromDatabase() {

        //get a database connection:
        dbOpener = new MyOpener(this);
        db = dbOpener.getWritableDatabase();

        // We want to get all of the columns. Look at MyOpener.java for the definitions:
        String[] columns = {MyOpener.COL_ID, MyOpener.COL_NAME, MyOpener.COL_DATE, MyOpener.COL_LON, MyOpener.COL_lAT};
        //query all the results from the database:
        Cursor results = db.query(false, MyOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        //Now the results object has rows of results that match the query.
        //find the column indices:
        int nameColumnIndex = results.getColumnIndex(MyOpener.COL_NAME);
        int dateColIndex = results.getColumnIndex(MyOpener.COL_DATE);
        int lonColIndex = results.getColumnIndex(MyOpener.COL_LON);
        int latColIndex = results.getColumnIndex(MyOpener.COL_lAT);
        int idColIndex = results.getColumnIndex(MyOpener.COL_ID);

        //iterate over the results, return true if there is a next item:
        while (results.moveToNext()) {
            String name = results.getString(nameColumnIndex);
            String date = results.getString(dateColIndex);
            float lon = results.getFloat(lonColIndex);
            float lat = results.getFloat(latColIndex);
            long id = results.getLong(idColIndex);

            //add all the messages to the arrayList
            list.add(new NasaImage(name, date, lon, lat, id));
        }


    }

    /**
     * The list adapter class that is used to display the items in the listview
     */
    private class ListAdapter extends BaseAdapter {

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            return list.get(position).getId();
        }

        public View getView(int position, View old, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();

            View newView = inflater.inflate(R.layout.preview_nasadb_image, parent, false);

            //set what the text should be for this row:
            TextView lonText = newView.findViewById(R.id.lonText);
            lonText.append(" " + list.get(position).getLon());

            //set what the text should be for this row:
            TextView latText = newView.findViewById(R.id.latText);
            latText.append(" " + list.get(position).getLat());

            //set what the text should be for this row:
            TextView dateText = newView.findViewById(R.id.dateText);
            dateText.append(" " + list.get(position).getDate());

            ImageView imageNasa = newView.findViewById(R.id.imageNasa);

            //opens the saved image on the hardware based on the unique name and sets the image view to this image
            FileInputStream fis = null;
            try {
                fis = openFileInput(list.get(position).getName());
            } catch (FileNotFoundException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            Bitmap image = BitmapFactory.decodeStream(fis);
            imageNasa.setImageBitmap(image);


            //return it to be put in the table
            return newView;
        }
    }

    /**
     * the class that represents one item in our listview
     */
    private class NasaImage {

        private String name;
        private String date;
        private float lon;
        private float lat;
        private long id;

        NasaImage(String name, String date, float lon, float lat, long id) {
            this.name = name;
            this.date = date;
            this.lon = lon;
            this.lat = lat;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public float getLon() {
            return lon;
        }

        public float getLat() {
            return lat;
        }

        public long getId() {
            return id;
        }
    }

    /**
     * the class is used to create and updatethe database on the device that stores the items
     */
    private class MyOpener extends SQLiteOpenHelper {

        protected static final String DATABASE_NAME = "Nasa_DB_Images";
        protected static final int VERSION_NUMBER = 1;
        public final static String TABLE_NAME = "Nasa_Images";
        public final static String COL_NAME = "Image_Name";
        public final static String COL_DATE = "Image_Date";
        public final static String COL_LON = "Longitude";
        public final static String COL_lAT = "Latitude";
        public final static String COL_ID = "_id";

        public MyOpener(Context ctx) {
            super(ctx, DATABASE_NAME, null, VERSION_NUMBER);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT,"
                    + COL_DATE + " TEXT,"
                    + COL_LON + " REAL,"
                    + COL_lAT + " REAL);"); // add or remove columns
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            onCreate(db);
        }
    }


}
