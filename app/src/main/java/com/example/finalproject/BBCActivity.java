package com.example.finalproject;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.finalproject.nasa_img_activity.NASAImgActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *  BBCActivity class which runs the BBC news reader application
 */
public class BBCActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Static variables for the data to be passed to fragment
    /**
     * BBC article title column identifier
     */
    public static final String ARTICLE_TITLE = "TITLE";
    /**
     * BBC article description column identifier
     */
    public static final String ARTICLE_DESCRIPTION = "DESCRIPTION";
    /**
     * BBC article date column identifier
     */
    public static final String ARTICLE_DATE = "DATE";
    /**
     * BBC article url column identifier
     */
    public static final String ARTICLE_URL = "URL";
    /**
     * BBC article ID column identifier
     */
    public static final String ARTICLE_ID = "ID";
    /**
     * BBC article favorite column identifier
     */
    public static final String ARTICLE_FAV = "FAV";
    /**
     * ArrayList for listview adapter that holds all articles in database
     */
    private ArrayList<Article> allArticleList = new ArrayList<>(Arrays.asList());
    /**
     * ArrayList for listview adapter that holds all favorite articles in database
     */
    private ArrayList<Article> favoriteArticleList = new ArrayList<>(Arrays.asList());
    /**
     * ListView adapter for all article display
     */
    private AllArticlesListAdapter allAdapter;
    /**
     * ListView adapter for favorite article display
     */
    private FavoriteArticlesListAdapter favoritesAdapter;
    /**
     * Database that contains all articles
     */
    SQLiteDatabase db;
    /**
     * Progress bar that displays progress while parsing the xml feed
     */
    private ProgressBar progressBar;
    /**
     * Shared preferences variable to save entered text between app sessions
     */
    private SharedPreferences prefs = null; // shared preferences variable used to store email

    /**
     * The onCreate method of the activity to create the activity page. Generates the data parser, listview generation
     * and all the listeners used in the BBC news reader application
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbc);

        // Get and load the toolbar with all links to other activities
        // This gets the toolbar from the layout:
        Toolbar tBar = (Toolbar) findViewById(R.id.bbc_toolBar);

        // This loads the toolbar, which calls onCreateOptionsMenu below:
        setSupportActionBar(tBar);

        // Retrieve the drawer layout for the navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Load the navigation drawer
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Hide the current BBC Activity from the drawer items
        navigationView.getMenu().findItem(R.id.bbc).setVisible(false);

        // Set the Navigation Drawer header text
        TextView info = navigationView.getHeaderView(0).findViewById(R.id.activityName);
        info.setText("BBC News Reader");
        info = navigationView.getHeaderView(0).findViewById(R.id.author);
        info.setText("Author: Mohamed El Sherif");
        info = navigationView.getHeaderView(0).findViewById(R.id.version);
        info.setText("Version: 1.2");

        // To make the icons colors visible in the drawer
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        // Load search text from previous app session into search bar
        prefs = getSharedPreferences("FileStrings", Context.MODE_PRIVATE);
        String searchText = prefs.getString("SEARCH", "");
        EditText editText = (EditText) findViewById(R.id.bbc_addMailText);
        editText.setText(searchText);

        // Create a new query object to query the BBC xml feed and obtain the data
        BBCQuery query = new BBCQuery();
        query.execute("http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");

        // Load any previous articles from the database into the articleList arraylist
        this.loadArticlesFromDatabase();

        //Store the layout view elements into variables
        Button showFavoritesBtn = findViewById(R.id.bbc_favoritesBtn);
        Button showAllBtn = findViewById(R.id.bbc_showAllBtn);
        ListView bbcListView = (ListView) findViewById(R.id.bbc_listView); //Store the BBC ListView in a variable
        progressBar = findViewById(R.id.bbc_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Instantiate the ListView adapters
        allAdapter = new AllArticlesListAdapter();
        favoritesAdapter = new FavoriteArticlesListAdapter();

        // When show all button is clicked, load all articles
        showAllBtn.setOnClickListener(click -> {
            Snackbar.make(click, getString(R.string.bbc_displayLatestSnackBar), Snackbar.LENGTH_SHORT).show();
            bbcListView.setAdapter(allAdapter);
        });

        // When favorites button is clicked, only load articles that are favorited
        showFavoritesBtn.setOnClickListener(click -> {
            Snackbar.make(click, getString(R.string.bbc_displayFavSnackBar), Snackbar.LENGTH_SHORT).show();
            bbcListView.setAdapter(favoritesAdapter);
        });

        bbcListView.setAdapter(allAdapter = new AllArticlesListAdapter());

        // A new alertDialogBuilder to be used by multiple features
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Click listener for articles. Allows user to favorite selected article
        bbcListView.setOnItemLongClickListener((parent, view, position, id) -> {

            // if the user is in all articles list view, then allow only to favorite articles
            if (bbcListView.getAdapter().equals(allAdapter)) {
                alertDialogBuilder.setTitle(getString(R.string.bbc_addFavAlertTitle))
                        .setMessage(getString(R.string.bbc_addToFavAlertMsg)).
                        setPositiveButton(getString(R.string.bbc_yes), (click, arg) -> {
                            this.favoriteArticle(id, true); // update favorite field in database
                            this.allArticleList.get(position).setFav(true); // change article fav property in arraylist
                            if (this.addToFavoritesList(this.allArticleList.get(position))) {   // add to favorites array list
                                allAdapter.notifyDataSetChanged();
                                favoritesAdapter.notifyDataSetChanged();
                                Toast.makeText(((Dialog) click).getContext(), getString(R.string.addFavToast), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(getString(R.string.bbc_no), (click, arg) -> {
                        }).create().show();
            }

            // else if user is in favorites list view, allow user to unfavorite selected articles only
            else {
                alertDialogBuilder.setTitle(getString(R.string.bbc_removeFavAlertTitle)).setMessage(getString(R.string.bbc_removeFavAlertMsg)).
                        setPositiveButton(getString(R.string.bbc_yes), (click, arg) -> {
                            this.favoriteArticle(id, false);    //update favorite field in database
                            this.allArticleList.get(position).setFav(false);    // change article fav property in arraylist
                            this.favoriteArticleList.remove(position);  // remove from favorites array list
                            allAdapter.notifyDataSetChanged();
                            favoritesAdapter.notifyDataSetChanged();
                            Toast.makeText(((Dialog) click).getContext(), getString(R.string.bbc_removeFavToast), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(getString(R.string.bbc_no), (click, arg) -> {
                        }).create().show();
            }
            return true;
        });

        // Add email to mailing list button click listener
        Button addEmailBtn = (Button) findViewById(R.id.bbc_addMailBtn);
        EditText emailEditText = (EditText) findViewById(R.id.bbc_addMailText);
        addEmailBtn.setOnClickListener((click) -> {
            Toast.makeText(this.getApplicationContext(), emailEditText.getText() +  " " + getString(R.string.bbc_emailToast), Toast.LENGTH_SHORT).show();
        });


        // Fragment for article details when user short clicks on an article
        bbcListView.setOnItemClickListener((list, view, position, id) -> {
            Bundle dataToPass = new Bundle();
            dataToPass.putString(ARTICLE_TITLE, this.allArticleList.get(position).getTitle());
            dataToPass.putString(ARTICLE_DESCRIPTION, this.allArticleList.get(position).getDescription());
            dataToPass.putString(ARTICLE_DATE, this.allArticleList.get(position).getDate());
            dataToPass.putString(ARTICLE_URL, this.allArticleList.get(position).getUrl());
            dataToPass.putLong(ARTICLE_ID, id);
            dataToPass.putBoolean(ARTICLE_FAV, this.allArticleList.get(position).isFav());

            Intent BBCphoneFragmentActivity = new Intent(BBCActivity.this, BBCphoneFragmentActivity.class);
            BBCphoneFragmentActivity.putExtras(dataToPass);
            startActivity(BBCphoneFragmentActivity);
        });

        // HELP BUTTON LISTENER
        // Shows an alert dialog that displays some help information for the user
        Button helpBtn = (Button) findViewById(R.id.bbc_helpBtn);
        helpBtn.setOnClickListener(btnClick -> {
            helpMessageAlert();
        });
    }

    /**
     * Generates an alert dialog displaying help tips to be used in conjunction with the help button
     * click listener
     */
    private void helpMessageAlert() {
        String helpMessage = getString(R.string.bbc_helpText1) +
                "\n" + getString(R.string.bbc_helpText2) +
                "\n" +  getString(R.string.bbc_helpText3) +
                "\n" + getString(R.string.bbc_helpText4) +
                "\n" + getString(R.string.bbc_helpText5);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.bbc_helpAlertTitle)
                .setCancelable(false)
                .setMessage(helpMessage).
                setPositiveButton(getString(R.string.bbc_OK), (click, arg) -> {
                })
                .create().show();
    }

    //***********************************THIS PORTION HANDLES SHARED PREFERENCES***************************//

    /**
     * Method called when the app is put on pause aka is allocated to the background
     */
    @Override
    protected void onPause() {
        super.onPause();
        EditText searchText = findViewById(R.id.bbc_addMailText);
        this.saveSharedPreferences(searchText.getText().toString());
    }

    /**
     * Method called when the app is stopped
     */
    @Override
    protected void onStop() {
        super.onStop();
        EditText searchText = findViewById(R.id.bbc_addMailText);
        this.saveSharedPreferences(searchText.getText().toString());
    }

    /**
     * Saves the argument string into a shared preferences variable and commits it
     *
     * @param searchText the string to be committed to shared preferences
     */
    private void saveSharedPreferences(String searchText) {
        prefs = getSharedPreferences("FileStrings", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("SEARCH", searchText);
        edit.commit();
    }

    //***********************************THIS PORTION HANDLES THE TOOLBAR MENU OPTION SELECTION***************************//

    /**
     * Called when creating the menu toolbar at the top of the application. Uses argument Menu object to inflate
     * the toolbar layout. Returns true if successful
     *
     * @param menu Menu object to inflate the toolbar layout
     * @return true if successfully created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);

        // Since the current activity is the BBC Activity, hide the BBC Activity option from the toolbar menu
        menu.findItem(R.id.bbc).setVisible(false);
        return true;
    }

    /**
     * Called when a toolbar menu option is selected. Jumps to another activity based on the selected
     * argument MenuItem item clicked
     *
     * @param item MenuItem object that is clicked
     * @return true if successful
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent nextActivity = null;

        switch (item.getItemId()) {
            //Toolbar menu option selection actions
            case R.id.home:
                finish();
                break;
            case R.id.nasa:
                nextActivity = new Intent(BBCActivity.this, NASAImgActivity.class);
                break;
            case R.id.nasadb:
                nextActivity = new Intent(BBCActivity.this, NASAdbActivity.class);
                break;
            case R.id.guardian:
                nextActivity = new Intent(BBCActivity.this, GuardianActivity.class);
                break;
            case R.id.help_item:
                this.helpMessageAlert();
                break;
        }

        if (nextActivity != null) {
            startActivity(nextActivity);
            finish();
        }

        return true;
    }

    //***************************** THIS PORTION IS FOR THE NAVIGATION DRAWER ITEM SELECTION*****************************************//

    /**
     * When an Navigation drawer MenuItem is selected, app jumps to activity corresponding to the
     * argument MenuItem item clicked
     *
     * @param item the MenuItem item clicked
     * @return true if successful
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent nextActivity = null;

        switch (item.getItemId()) {
            // Navigation drawer item selection actions
            case R.id.home:
                this.finish();
                break;
            case R.id.nasa:
                nextActivity = new Intent(BBCActivity.this, NASAImgActivity.class);
                break;
            case R.id.nasadb:
                nextActivity = new Intent(BBCActivity.this, NASAdbActivity.class);
                break;
            case R.id.guardian:
                nextActivity = new Intent(BBCActivity.this, GuardianActivity.class);
                break;
            case R.id.help_item:
                this.helpMessageAlert();
                break;
        }

        if (nextActivity != null) {
            startActivity(nextActivity);
            finish();
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }

    //******************************* THIS PORTION IS FOR XML PARSING *****************************************//

    /**
     * BBCQuery class used to generate the queries required to pull data from the BBC xml newsfeed
     */
    private class BBCQuery extends AsyncTask<String, Integer, String> {

        private String title;
        private String description;
        private String date;
        private String link;


        /**
         * Establishes an HTTP connection and parses the desired the desired url for the specified xml tags
         * to create new Article objects and add them to the database
         *
         * @param args
         * @return a string
         */
        @Override
        protected String doInBackground(String... args) {
            try {
                //create a URL object of what server to contact:
                URL url = new URL(args[0]);
                //open the connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //wait for data:
                InputStream response = urlConnection.getInputStream();

                //Create pull parser here
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(response, "UTF-8");

                String parameter = null;    // test parameter used to check if value parsed is correct

                int eventType = xpp.getEventType(); //The parser is currently at START_DOCUMENT
                boolean itemFound = false;

                // Loop over the xml to pull values based on desired tag
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.END_TAG) {
                        if (xpp.getName().equals("item")) {
                            insertArticleDB(title, description, date, link, false);
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        //If you get here, then you are pointing at a start tag
                        parameter = xpp.getName();
                        if (parameter.equals("item")) {
                            itemFound = true;
                        } else if (parameter.equals("title")) {
                            if (itemFound) {
                                xpp.next();
                                title = xpp.getText();
                                Log.d("Pull Test", "Title " + title);
                                publishProgress(25);
                            }
                        } else if (parameter.equals("description")) {
                            if (itemFound) {
                                xpp.next();
                                description = xpp.getText();
                                Log.d("Pull Test", "Description " + description);
                                publishProgress(50);
                            }
                        } else if (parameter.equals("link")) {
                            if (itemFound) {
                                xpp.next();
                                link = xpp.getText();
                                Log.d("Pull Test", "URL " + link);
                                publishProgress(75);
                            }
                        } else if (parameter.equals("pubDate")) {
                            if (itemFound) {
                                xpp.next();
                                date = xpp.getText();
                                Log.d("Pull Test", "Date " + date);
                                publishProgress(100);
                            }
                        }
                    }
                    eventType = xpp.next();
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return "Done";
        }

        /**
         * Displays a progress bar that increases as the pull parser parses data from the url
         *
         * @param args
         */
        public void onProgressUpdate(Integer... args) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(args[0]);
        }

        /**
         * Once the DoInBackground method is finished (the pull parser completed parsing and pulling of data),
         * the progress bar is made invisible
         *
         * @param fromDoInBackground
         */
        public void onPostExecute(String fromDoInBackground) {
            Log.i("HTTP", fromDoInBackground);
            progressBar.setVisibility(View.INVISIBLE);
        }

    }

    //******************************* THIS PORTION IS FOR DATABASE HANDLING ************************************//
    // List Adapter for the list view that will display favorited articles in the database

    /**
     * FavoriteArticlesListAdapter class that extends the BaseAdapter superclass. Used to create
     * a list adapter for the favorited articles list view
     */
    private class FavoriteArticlesListAdapter extends BaseAdapter {

        /**
         * Returns the size of the favoriteArticleList array
         *
         * @return an integer representing the size of the favoriteArticleList array
         */
        @Override
        // returns the number of rows that will be in your listview. In this case
        // it should be the number of strings in the array list object
        public int getCount() {
            return favoriteArticleList.size();
        }

        /**
         * Returns the Article object from the favorites arraylist at the argument index
         *
         * @param position integer representing the index of the Article object
         * @return the Article object
         */
        // Returns the item to show in the list at the specified position
        @Override
        public Article getItem(int position) {
            return favoriteArticleList.get(position);
        }

        /**
         * Returns the view of the layout that will be positioned at the specified row in the listview
         *
         * @param position within the arraylist
         * @param convertView the new view to be adopted
         * @param parent the parent view that contains the view
         * @return the view
         */
        // Returns the layout that will be positioned at the specified row in the list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            // set what was sent or received
            View newView = null;
            TextView tView = null;
            Article article = this.getItem(position);

            // Inflate the list view with newView and get the textView to hold the article listing
            newView = inflater.inflate(R.layout.bbc_article_listing, parent, false);
            tView = newView.findViewById(R.id.bbcArticleTextView);
            // finally set the textView within the listview with the headline of the article
            tView.setText(article.getTitle());

            // return the new view created to be put in the table
            return newView;
        }

        /**
         * Returns the database id of the Article at the argument position within the arraylist
         *
         * @param position the index of the Article object within the arraylist
         * @return the database ID
         */
        // This is the database id of the item at position.
        public long getItemId(int position) {
            return getItem(position).getId();
        }
    }

    /**
     * AllArticlesListAdapter class that extends the BaseAdapter superclass. Used to create
     * a list adapter for the all articles list view
     */
    // List Adapter for the list view that will display all articles in the database
    private class AllArticlesListAdapter extends BaseAdapter {
        @Override
        // returns the number of rows that will be in your listview. In this case
        // it should be the number of strings in the array list object
        public int getCount() {
            return allArticleList.size();
        }

        /**
         * Returns the size of the allArticleList array
         *
         * @return an integer representing the size of the allArticleList array
         */
        // Returns the item to show in the list at the specified position
        @Override
        public Article getItem(int position) {
            return allArticleList.get(position);
        }

        /**
         * Returns the view of the layout that will be positioned at the specified row in the listview
         *
         * @param position within the arraylist
         * @param convertView the new view to be adopted
         * @param parent the parent view that contains the view
         * @return the view
         */
        // Returns the layout that will be positioned at the specified row in the list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            // set what was sent or received
            View newView = null;
            TextView tView = null;
            Article article = this.getItem(position);

            // Inflate the list view with newView and get the textView to hold the article listing
            newView = inflater.inflate(R.layout.bbc_article_listing, parent, false);
            tView = newView.findViewById(R.id.bbcArticleTextView);
            // finally set the textView within the listview with the headline of the article
            tView.setText(article.getTitle());

            // return the new view created to be put in the table
            return newView;
        }

        /**
         * Returns the database id of the Article at the argument position within the arraylist
         *
         * @param position the index of the Article object within the arraylist
         * @return the database ID
         */
        // This is the database id of the item at position.
        public long getItemId(int position) {
            return getItem(position).getId();
        }
    }

    /**
     * Checks if the argument article already exists in the favorite articles arraylist. If it does not,
     * add it to the list
     *
     * @param article to be added to favorites
     * @return true if added, else false
     */
    // Checks if article that is favorited already exists in favorites list. If no duplicate, adds to the list
    private boolean addToFavoritesList(Article article) {
        boolean success = true;
        for (Article art : this.favoriteArticleList) {
            if (article.equals(art)) {
                success = false;
            }
        }
        if (!success) {
            return false;
        } else {
            this.favoriteArticleList.add(article);
        }
        return true;
    }

    /**
     * Loads all the articles from the database into both the all articles arraylist and the
     * favorite articles arraylist
     */
    // Load all the articles in the database into the arrayList articleList
    // to load into ListView. If argument is true, load only favorited articles. Else load all
    private void loadArticlesFromDatabase() {
        BBCOpener dbOpener = new BBCOpener(this);
        db = dbOpener.getWritableDatabase();

        // All columns of the database. We store them in an array
        String[] columns = {BBCOpener.COL_ID, BBCOpener.COL_TITLE, BBCOpener.COL_DESCRIPTION, BBCOpener.COL_DATE, BBCOpener.COL_URL, BBCOpener.COL_FAV};

        // Query all results from the database
        Cursor results = db.query(false, BBCOpener.TABLE_NAME, columns, null, null, null, null, null, null);
        this.printCursor(results, db.getVersion());

        // Results object has rows of results that match the query
        // Get column indices
        int titleColIndex = results.getColumnIndex(BBCOpener.COL_TITLE);
        int descriptionColIndex = results.getColumnIndex(BBCOpener.COL_DESCRIPTION);
        int dateColIndex = results.getColumnIndex(BBCOpener.COL_DATE);
        int urlColIndex = results.getColumnIndex(BBCOpener.COL_URL);
        int idColIndex = results.getColumnIndex(BBCOpener.COL_ID);
        int favColIndex = results.getColumnIndex(BBCOpener.COL_FAV);

        // Iterate over the query results, and add each row of fields to the arraylist
        while (!results.isAfterLast()) {
            String title = results.getString(titleColIndex);
            String description = results.getString(descriptionColIndex);
            String date = results.getString(dateColIndex);
            String url = results.getString(urlColIndex);
            long id = results.getLong(idColIndex);
            int fav = results.getInt(favColIndex);

            // If the article is favortied, then article fav field = true
            boolean favType = false;
            if (fav == 1) {
                favType = true;
            } else {
                favType = false;
            }

            // if article is favorited, add to favorites list as well
            if (favType) {
                this.favoriteArticleList.add(new Article(id, title, description, date, url, favType));
            }

            // Add all articles to Articles ArrayList
            this.allArticleList.add(new Article(id, title, description, date, url, favType));

            results.moveToNext();
        }
    }

    /**
     * Creates and inserts a new Article object into the database
     *
     * @param title title of the article
     * @param description description of the article
     * @param date date of the article
     * @param url url of the article
     * @param favType indicator if article is favorited
     * @return return the database id of inserted row
     */
    // Adds the article's parameters (in argument) as a row to the database fields
    private long insertArticleDB(String title, String description, String date, String url, boolean favType) {
        // Add to database and get the new ID
        ContentValues newRowValues = new ContentValues();
        // Provide value for fields defined in the BBCOpener class
        newRowValues.put(BBCOpener.COL_TITLE, title);
        newRowValues.put(BBCOpener.COL_DESCRIPTION, description);
        newRowValues.put(BBCOpener.COL_DATE, date);
        newRowValues.put(BBCOpener.COL_URL, url);

        // if favType = 1, user favorited the article
        if (favType) {
            newRowValues.put(BBCOpener.COL_FAV, 1);
        } else {
            newRowValues.put(BBCOpener.COL_FAV, 0);
        }

        // finally insert into the database
        long newRowId = db.insert(BBCOpener.TABLE_NAME, null, newRowValues);
        return newRowId;
    }

    /**
     * Update the selected row in the database to indicate it has been favorited or unfavorited
     *
     * @param id the id of the row to be updated
     * @param fav boolean true if favorited, false if unfavorited
     */
    // Update the selected row from the database. if bool arg true, fav. Else unfav
    private void favoriteArticle(long id, boolean fav) {
        ContentValues cv = new ContentValues();
        if (fav) {
            cv.put(BBCOpener.COL_FAV, 1);
        } else {
            cv.put(BBCOpener.COL_FAV, 0);
        }
        db.update(BBCOpener.TABLE_NAME, cv, "_id=?", new String[]{Long.toString(id)});
    }

    /**
     * Loops through database rows to check if the data obtained is correct
     *
     * @param c the Cursor used to hold results
     * @param version current version of the database
     */
    // Helper method to check if results obtained from database are correct
    private void printCursor(Cursor c, int version) {
        c.moveToFirst();
        Log.d("Version", Integer.toString(version));    // log version number of db
        Log.d("ColumnCount", Integer.toString(c.getColumnCount())); // log number of columns in db

        for (int i = 0; i < c.getColumnNames().length; i++) {
            Log.d("Column Name", c.getColumnNames()[i]);
        }
        Log.d("Number of rows", "" + c.getCount());

        int colTitleIndex = c.getColumnIndex("TITLE");
        int colDescriptionIndex = c.getColumnIndex("DESCRIPTION");
        int colDateIndex = c.getColumnIndex("DATE");
        int colUrlIndex = c.getColumnIndex("URL");
        int colFavIndex = c.getColumnIndex("FAVORITE");

        while (!c.isAfterLast()) {
            String title = c.getString(colTitleIndex);
            String description = c.getString(colDescriptionIndex);
            String date = c.getString(colDateIndex);
            String url = c.getString(colUrlIndex);
            int fav = c.getInt(colFavIndex);
            Log.d("Article properties", "Title: " + title + " Description: " + description + " Date: " + date + " Url: " + url + " Favorited: " + fav);
            c.moveToNext();
        }
        c.moveToFirst();
    }

}