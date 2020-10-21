package com.example.finalproject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.nasa_img_activity.NASAImgActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GuardianActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    SharedPreferences prefs = null;

    private ProgressBar bar;

    /**
     *The list of Guardian Articles that the user can access
     */
    private ArrayList<GuardianArticle> articles = new ArrayList<>();

    /**
     * The list of Guardian Articles the user has favourited
     */
    private ArrayList<GuardianArticle> favourites = new ArrayList<>();

    /**
     * A ListAdapter that populates a ListView with GuardianArticles
     */
    private MyListAdapter adapter;

    /**
     * A list adapter to populate a ListView with favourites
     */
    private MyListAdapter favouritesAdapter;

    /**
     * An object that allows the application to access a SQL Lite
     * database that holds data of every GuardianArticle
     */
    private SQLiteDatabase db;

    /**
     * The ListView that displays all GuardianArticles
     */
    private ListView titleList;

    /**
     * Determines whether the list is a favourites list of not
     */
    private boolean isFavouritesList = false;

    /**
     *
     * @param savedInstanceState
     * A previously saved instance to be loaded upon creation of this
     * Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian);

        //Load the search done when app was paused
        prefs = getSharedPreferences("Guardian", Context.MODE_PRIVATE);

        String searchTerm = prefs.getString("searchTerm", null);

        if(searchTerm != null && !searchTerm.isEmpty()){
            executeQuery(searchTerm);
        }

        //Set the Toolbar
        Toolbar tBar = (Toolbar)findViewById(R.id.guardian_toolBar);

        setSupportActionBar(tBar);

        //Set the DrawerLayout
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Load nav drawer
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Hide Guardian Activity from among the drawer items
        navigationView.getMenu().findItem(R.id.guardian).setVisible(false);

        // Set header text
        TextView info = navigationView.getHeaderView(0).findViewById(R.id.activityName);
        info.setText(R.string.guardian);
        info = navigationView.getHeaderView(0).findViewById(R.id.author);
        info.setText(R.string.guardian_author);
        info = navigationView.getHeaderView(0).findViewById(R.id.version);
        info.setText(R.string.versionNum);

        // Make colors visible in drawer
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        //Set search adapter
        adapter = new MyListAdapter();

        titleList = findViewById(R.id.titles);

        titleList.setAdapter(adapter);


        //Load favourites from database
        favouritesAdapter = new MyListAdapter();

        loadFavouritesFromDatabase();

        favouritesAdapter.setElements(favourites);

        favouritesAdapter.notifyDataSetChanged();

        //Make ProgressBar visible
        bar = findViewById(R.id.article_bar);
        bar.setVisibility(View.VISIBLE);


        //Add the button to search for articles
        Button searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener(bt -> {
            EditText text = findViewById(R.id.searchText);

            String buttonSearchTerm = text.getText().toString();

            executeQuery(buttonSearchTerm);


        });


        //Show detailed information when item is clicked
        titleList.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id)-> {

            GuardianArticle article = (GuardianArticle) parent.getItemAtPosition(position);

            Toast.makeText(this, R.string.title + article.getTitle() + "\n"
                    + R.string.url + article.getSectionName() + "\n"
                    + R.string.section_name + article.getURL(), Toast.LENGTH_SHORT).show();
        });

        //Allows user to remove from favourites, add to favourites, or go to fragment for detailed information
        titleList.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id)-> {

            if(isFavouritesList){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                alertDialogBuilder.setTitle(R.string.delete)
                        .setMessage(R.string.delete_this)
                        .setPositiveButton(R.string.yes, (click, arg)->{
                            deleteArticle(favouritesAdapter.getItem(position));
                        })
                        .setNegativeButton(R.string.bbc_no, (click, arg)->{}).create().show();
            }else{
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                alertDialogBuilder.setTitle(R.string.add_favourites)
                        .setMessage(R.string.bbc_addToFavAlertMsg)
                        .setPositiveButton(R.string.yes, (click, arg)->{
                            addToFavourites(adapter.getItem(position));
                        }).setNeutralButton(R.string.details, (click, arg)->{
                            Bundle dataToPass = new Bundle();
                            GuardianArticle article = (GuardianArticle) parent.getItemAtPosition(position);

                            dataToPass.putString("name", article.getTitle());
                            dataToPass.putString("url", article.getURL());
                            dataToPass.putString("sectionName", article.getSectionName());

                            Intent goToGuardianFavouritesFragment = new Intent(GuardianActivity.this, GuardianFragmentActivity.class);
                            goToGuardianFavouritesFragment.putExtras(dataToPass);
                            startActivity(goToGuardianFavouritesFragment);
                        })
                        .setNegativeButton(R.string.bbc_no, (click, arg)->{}).create().show();
            }



            return true;
        });

        //Sets the help button
        Button helpButton = (Button) findViewById(R.id.help_button);

        helpButton.setOnClickListener(bt -> {
            helpMessageAlert();
        });

        //Shows favourites
        Button favouritesButton = findViewById(R.id.guardian_favourites_button);

        favouritesButton.setOnClickListener(b->{
            titleList.setAdapter(this.favouritesAdapter);

            isFavouritesList = true;

            Snackbar sb = Snackbar.make(favouritesButton, getResources().getString(R.string.view_favorites), Snackbar.LENGTH_SHORT);
            sb.show();
        });

    }

    /**
     * Creates an ArticleQuery to run an async task
     * Searches The Guardian for articles
     *
     * @param term
     * The search term to be used in the query
     */
    private void executeQuery(String term){
        ArticleQuery query = new ArticleQuery();

        String url = "https://content.guardianapis.com/search?api-key=1fb36b70-1588-4259-b703-2570ea1fac6a&q=" + term;
        query.execute(url);
    }

    /**
     * Creates the help message alert when the help button is clicked
     */
    private void helpMessageAlert() {
        String helpMessage = getResources().getString(R.string.guardian_help);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.bbc_helpAlertTitle)
                .setCancelable(false)
                .setMessage(helpMessage).
                setPositiveButton(R.string.ok, (click, arg) -> {
                })
                .create().show();
    }

    /**
     *  Calls saveSharedPreferences when this activity is paused
     */
    @Override
    protected void onPause(){
        super.onPause();

        EditText text = findViewById(R.id.searchText);

        this.saveSharedPreferences(text.getText().toString());
    }

    /**
     * Saves the search text to SharedPreferences
     *
     * @param searchTerm
     * The search term to be saved
     */
    private void saveSharedPreferences(String searchTerm){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("searchTerm", searchTerm);
        editor.apply();
    }

    /**
     *
     * @param menu
     * Creates the menu toolbar when options menu is created
     *
     * @return
     * Whether or not inflation was successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);

        // Since the current activity is the BBC Activity, hide the BBC Activity option from the toolbar menu
        menu.findItem(R.id.guardian).setVisible(false);
        return true;
    }

    /**
     *
     * Goes to the corresponding page when the icon is clicked
     *
     * @param item
     * The MenuItem clicked
     *
     * @return
     * True when activity begins
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent nextActivity=null;

        switch(item.getItemId())
        {
            //Toolbar menu option selection actions
            case R.id.home:
                finish();
                break;
            case R.id.nasa:
                nextActivity = new Intent(GuardianActivity.this, NASAImgActivity.class);
                break;
            case R.id.nasadb:
                nextActivity = new Intent(GuardianActivity.this, NASAdbActivity.class);
                break;
            case R.id.bbc:
                nextActivity = new Intent(GuardianActivity.this, BBCActivity.class);
                break;
            case R.id.help_item:
                this.helpMessageAlert();
                break;
        }

        if(nextActivity != null){
            startActivity(nextActivity);
            finish();
        }

        return true;
    }

    //***************************** THIS PORTION IS FOR THE NAVIGATION DRAWER ITEM SELECTION*****************************************//

    /**
     *
     * Goes to the corresponding page when the icon is clicked
     *
     * @param item
     * The MenuItem clicked
     *
     * @return
     * True when activity begins
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent nextActivity=null;

        switch(item.getItemId())
        {
            // Navigation drawer item selection actions
            case R.id.home:
                this.finish();
                break;
            case R.id.nasa:
                nextActivity = new Intent(GuardianActivity.this, NASAImgActivity.class);
                break;
            case R.id.nasadb:
                nextActivity = new Intent(GuardianActivity.this, NASAdbActivity.class);
                break;
            case R.id.bbc:
                nextActivity = new Intent(GuardianActivity.this, BBCActivity.class);
                break;
            case R.id.help_item:
                this.helpMessageAlert();
                break;
        }

        if(nextActivity != null){
            startActivity(nextActivity);
            finish();
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }



    //***************************** THIS PORTION IS FOR THE LIST ADAPTER  **********************************************//
    private class MyListAdapter extends BaseAdapter {

        /**
         * A list containing GuardianArticles to be
         * added to the ListView
         */
        private ArrayList<GuardianArticle> elements;

        /**
         * Creates a new MyListAdapter object and
         * initializes the elements array
         */
        private MyListAdapter() {
            elements = articles;
        }


        private void setElements(ArrayList<GuardianArticle> elements){
            this.elements = elements;
        }
        /**
         *Adds a GuardianArticle object to the elements array
         *
         * @param s
         * The GuardianArticle to be added to the list
         */
        private void addElement(GuardianArticle s) {this.elements.add(s);}

        /**
         *
         * @return
         * The number of elements in the elements array
         */
        @Override
        public int getCount() {
            return this.elements.size();
        }

        /**
         *
         * @param position
         * The position of the GuardianArticle in the elements array
         *
         * @return
         * The GuardianArticle at the given position
         */
        @Override
        public GuardianArticle getItem(int position){
            return this.elements.get(position);
        }

        /**
         *
         * @param position
         * The position of the GuardianArticle in the elements array
         *
         * @return
         * The database id of the GuardianArticle at the given position
         */
        @Override
        public long getItemId(int position) {
            return this.elements.get(position).getId();
        }

        /**
         *
         * @param position
         * @param old
         * @param parent
         * @return
         */
        @Override
        public View getView(int position, View old, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();

            View newView;

            newView = inflater.inflate(R.layout.article_layout, parent, false);

            TextView articleView = newView.findViewById(R.id.article_title);

            articleView.setText(getItem(position).getTitle());

            return newView;
        }

    }

    //***************************** THIS PORTION IS FOR THE DATABASE **********************************************//

    /**
     *Loads all favourites articles from the database into the favourites ArrayList
     */
    private void loadFavouritesFromDatabase(){
        GuardianDatabaseOpener dbOpener = new GuardianDatabaseOpener(this);
        db = dbOpener.getWritableDatabase();

        String[] columns = {GuardianDatabaseOpener.COL_ID, GuardianDatabaseOpener.COL_TITLE, GuardianDatabaseOpener.COL_URL, GuardianDatabaseOpener.COL_SECTION_NAME};

        Cursor results = db.query(false, GuardianDatabaseOpener.TABLE_NAME, columns, null, null, null, null, null, null, null);

        printCursor(results, db.getVersion());

        int titleColumnIndex = results.getColumnIndex(GuardianDatabaseOpener.COL_TITLE);
        int urlColumnIndex = results.getColumnIndex(GuardianDatabaseOpener.COL_URL);
        int sectionColumnIndex = results.getColumnIndex(GuardianDatabaseOpener.COL_SECTION_NAME);
        int idColumnIndex = results.getColumnIndex(GuardianDatabaseOpener.COL_ID);

        while(results.moveToNext()){
            String title = results.getString(titleColumnIndex);
            String url = results.getString(urlColumnIndex);
            String sectionName = results.getString(sectionColumnIndex);
            long id = results.getLong(idColumnIndex);

            favourites.add(new GuardianArticle(title, url, sectionName, id));

        }

    }

    /**
     * Adds an article to the database and the favourites ArrayList
     * @param ga
     * The guardian article to be added
     */
    public void addToFavourites(GuardianArticle ga){
        addArticleToDatabase(ga);

        favourites.add(ga);
        favouritesAdapter.notifyDataSetChanged();



    }

    /**
     * Adds a GuardianArticle to the database
     * @param ga
     * The GuardianARticle to be added
     */
    private void addArticleToDatabase(GuardianArticle ga){

        ContentValues newRowValues = new ContentValues();

        newRowValues.put(GuardianDatabaseOpener.COL_TITLE, ga.getTitle());
        newRowValues.put(GuardianDatabaseOpener.COL_URL, ga.getURL());
        newRowValues.put(GuardianDatabaseOpener.COL_SECTION_NAME, ga.getSectionName());

        long id = db.insert(GuardianDatabaseOpener.TABLE_NAME, null, newRowValues);
        ga.setId(id);

    }



    /**
     *
     * Deletes a GuardianArticle from database db and removes it from the favourites list
     *
     * @param ga
     * The GuardianArticle to be deleted from the database
     */
    private void deleteArticle(GuardianArticle ga){
        db.delete(GuardianDatabaseOpener.TABLE_NAME, GuardianDatabaseOpener.COL_ID + "= ?", new String[] {Long.toString(ga.getId())});
        favourites.remove(ga);
        favouritesAdapter.notifyDataSetChanged();
    }

    /**
     *Uses a Cursor object to print the database version number,
     * and each row and column of the GuardianArticle table
     *
     * @param c
     * The Cursor to be printed
     *
     * @param version
     * The version number of the database
     */
    private void printCursor(Cursor c, int version){

        Log.i("Version Number",Integer.toString(version));

        Log.i("Number of columns", Integer.toString(c.getColumnCount()));


        String columns = "";

        for(String column: c.getColumnNames()){
            columns += column + "\n";
        }

        Log.i("Columns", columns);


        int titleColumnIndex = c.getColumnIndex(GuardianDatabaseOpener.COL_TITLE);
        int urlColumnIndex = c.getColumnIndex(GuardianDatabaseOpener.COL_URL);
        int sectionColumnIndex = c.getColumnIndex(GuardianDatabaseOpener.COL_SECTION_NAME);
        int idColumnIndex = c.getColumnIndex(GuardianDatabaseOpener.COL_ID);


        String rows = "";

        while(c.moveToNext()){

            rows += "Title: " + c.getString(titleColumnIndex) +
                    ", URL: " + c.getInt(urlColumnIndex) +
                    ", Section Name: " + c.getInt(sectionColumnIndex) +
                    ", ID: " + c.getLong(idColumnIndex) + "\n";

        }



        Log.i("Number of results", rows);


        System.out.println("\n");

        System.out.println("Number of results: " + c.getCount());

        System.out.println("Rows: ");


        c.moveToFirst();

    }


    private class ArticleQuery extends AsyncTask<String, Integer, String> {

        /**
         * A list of GuardianArticles that is later saved to the articles class variable
         */
        private ArrayList<GuardianArticle> guardianArticles = new ArrayList<>();

        /**
         * Background downloads GuardianArticles from The Guardian website
         *
         * @param args
         * @return
         * Done when the AsyncTask is done
         */
        protected String doInBackground(String... args) {

            try {

                URL url = new URL(args[0]);


                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream response = urlConnection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                //xpp.setInput(response, "UTF-8");

                BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                String result = sb.toString();

                System.out.println(result);//result is the whole string


                // convert string to JSON:
                JSONObject searchPage = new JSONObject(result);

                JSONObject jResponse = searchPage.getJSONObject("response");

                JSONArray results = jResponse.getJSONArray("results");

                for(int i = 0; i < results.length(); i++){
                    JSONObject item = results.getJSONObject(i);
                    articles.add(new GuardianArticle(item.getString("webTitle"), item.getString("webUrl"), item.getString("sectionName"), -1));
                    publishProgress(100*i/results.length());
                }

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }

            return "Done";
        }

        /**
         * Updates the ProgressBar
         * @param args
         */
        public void onProgressUpdate(Integer... args) {
            bar.setVisibility(View.VISIBLE);

            bar.setProgress(args[0]);
        }


        /**
         * Updates the GuardianArticles and makes the ProgressBar invisible
         * @param fromDoInBackground
         */
        public void onPostExecute(String fromDoInBackground) {

            articles = guardianArticles;

            adapter.notifyDataSetChanged();

            bar.setVisibility(View.INVISIBLE);
        }

        /**
         *
         * @param fName
         * The name of the file searched for
         * @return
         * Whether or not the file exists
         */
        private boolean fileExistence(String fName) {
            File file = getBaseContext().getFileStreamPath(fName);
            return file.exists();
        }
    }



}

