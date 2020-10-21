package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * The class represents the fragment that
 */
public class NasadbFragment extends Fragment {

    public static final String IMAGE_DATE = "imageDate";
    public static final String IMAGE_NAME = "imageName";
    private boolean isFavorite = false;
    private AppCompatActivity parentActivity;
    private TextView lon, lat, dateText;
    private ProgressBar progressBar;
    private ImageView imageNasa;
    private String imgDate, imgUrl, imgNameId;

    /**
     * the method gets called when the activity is first created
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       Bundle dataFromActivity = getArguments();


        // Inflate the layout for this fragment
        View result =  inflater.inflate(R.layout.fragment_nasadb, container, false);

        //display the longtudide
        lon = (TextView)result.findViewById(R.id.lon);
        float longitude = dataFromActivity.getFloat(NASAdbActivity.LONGITUDE); //gets the longitude provided
        lon.append(" "+ longitude);

        //display the latitude
        lat = (TextView)result.findViewById(R.id.lat);
        float latitude = dataFromActivity.getFloat(NASAdbActivity.LATITUDE); //gets the latitude provided
        lat.append(" "+latitude);

        dateText = (TextView)result.findViewById(R.id.date);
        progressBar = (ProgressBar)result.findViewById(R.id.progressBar);
        imageNasa = (ImageView)result.findViewById(R.id.imageNasa);

        //get a Asyn connection
        MyHTTPRequest req = new MyHTTPRequest();
        req.execute("https://api.nasa.gov/planetary/earth/imagery/?lon=" + longitude + "&lat="+ latitude +"&api_key=DOgrBY3hkcSxx5QJJrZTqOSKaRgnCp9fLy561C4P");


        // get the finish button, and add a click listener:
        Button finishButton = (Button)result.findViewById(R.id.finishButton);
        finishButton.setOnClickListener( clk -> {

            //Tell the parent activity to remove
            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
        });


        // get the Add to favorites button, and add a click listener:
        Button favoriteButton = (Button)result.findViewById(R.id.addFavourites);
        favoriteButton.setOnClickListener( clk -> {

            //only add the item to the favorites if an image is found
            if(imgNameId != null){

                //set it to true so the image won't get deleted if the user wishes to save it to favorites
                isFavorite =true;

                //the bundle added and sent to favorite list activity to be added
                Bundle dataToPass = new Bundle();
                dataToPass.putFloat(NASAdbActivity.LONGITUDE, longitude);
                dataToPass.putFloat(NASAdbActivity.LATITUDE, latitude);
                dataToPass.putString(IMAGE_DATE, imgDate);
                dataToPass.putString(IMAGE_NAME, imgNameId);

                //after sending the data to the favorite activity we are sent there
                Intent nextActivity = new Intent(parentActivity , NasadbListActivity.class);
                nextActivity.putExtras(dataToPass);
                startActivity(nextActivity);
                //Tell the parent activity to remove the fragment
                parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            } else {
                //displays a toast if there is no image to add
                Toast.makeText(parentActivity, R.string.cant_favorite, Toast.LENGTH_LONG).show();
            }

        });

        return result;
    }

    /**
     * the method is executed when the fragment is attached and sets the paratent activity
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //context will be the main nasa imagery activity
        parentActivity = (AppCompatActivity)context;
    }

    /**
     * when the fragment is stopped we delete the image so we save memory
     */
    @Override
    public void onStop() {
        super.onStop();
        //deletes the image from the hardware after the fragment has been stopped unless the user chooses to save it to favorites
        if(!isFavorite && imgNameId != null){
            fileDelete(imgNameId);
        }

    }

    /**
     * deletes the image from from the hardware
     * @param fName
     */
    public void fileDelete(String fName){
        File file = parentActivity.getBaseContext().getFileStreamPath(fName);
        file.delete();
    }

    /**
     * this classed is used to make a connection to the internet while the activity and the fragment is still running
     */
    private class MyHTTPRequest extends AsyncTask< String, Integer, String>
    {
        Bitmap image;

        /**
         * the main method were all the work is done in the background
         * @param args
         * @return
         */
        public String doInBackground(String ... args)
        {
            try {

                //create a URL object of what server to contact:
                URL url = new URL(args[0]);

                //open the connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //wait for data:
                InputStream response = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                String result = sb.toString(); //result is the whole string


                // convert string to JSON:
                JSONObject imgInfo = new JSONObject(result);



                    // get the string for image date and time and only take the date part
                    imgDate =imgInfo.getString("date").split("T")[0];
                    publishProgress(25);

                    //used to get the unique thumb id for each image from the image url
                    imgUrl = imgInfo.getString("url");
                    imgNameId = imgUrl.split("=")[1].split("&")[0];
                    publishProgress(50);

                    //checks if file already exists on the hardware or not using the unique thumbid
                    if(!fileExistance(imgNameId)){
                        HttpURLConnection connection;
                        image = null;
                        URL imageUrl = new URL(imgUrl);
                        connection = (HttpURLConnection) imageUrl.openConnection();
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200) {
                            image = BitmapFactory.decodeStream(connection.getInputStream());
                        }
                        FileOutputStream outputStream = parentActivity.openFileOutput(imgNameId, Context.MODE_PRIVATE);
                        image.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
                        outputStream.flush();
                        outputStream.close();

                        Log.i("Image download", "Image downloaded from website");
                    } else {
                        //if the image is found on the hardware means it was already a favorite so we set isFavorite to true to not delete it on exit
                        isFavorite = true;
                        FileInputStream fis = null;
                        try {    fis = parentActivity.openFileInput(imgNameId);   }
                        catch (FileNotFoundException e) {    e.printStackTrace();  }
                        image = BitmapFactory.decodeStream(fis);
                        Log.i("Image download", "Image found locally");
                    }
                    publishProgress(100);

            }
            catch (Exception e)
            {
                imgDate = getString(R.string.no_image);
                image = null;

                Log.e("Error", e.getMessage());
            }

            return "Done";
        }

        /**
         * checks if the file exist on the hardware or not
         * @param fName
         * @return
         */
        public boolean fileExistance(String fName){
            File file = parentActivity.getBaseContext().getFileStreamPath(fName);
            return file.exists();   }


        /**
         * used to update the gui while the work is done on the background
         * @param values
         */
        public void onProgressUpdate(Integer ... values)
        {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(values[0]);
        }

        /**
         * used to the execute after the end of the do in background method
         * @param fromDoInBackground
         */
        public void onPostExecute(String fromDoInBackground)
        {
            dateText.append( " " + imgDate);
            imageNasa.setImageBitmap(image);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
