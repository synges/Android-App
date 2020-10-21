package com.example.finalproject.nasa_img_activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;

import java.io.Serializable;

/**
 * Holds data from a row in the database
 *
 * @author Noah Cheesman
 * @version "%I%, %G%"
 */
class ImgData extends AppCompatActivity implements Serializable {
	private String description;
	private String imageUrl;
	private String date;
	private String title;
	private long id;

	/**
	 * Constructor to assign values to all class fields
	 *
	 * @param id          Id of the row in the database
	 * @param title       Title of the image
	 * @param image       Url of the image
	 * @param description Description of the image
	 * @param date        Date image was chosen as image of the day
	 */
	public ImgData(long id, String title, String image, String description, String date) {
		this.description = description;
		this.imageUrl = image;
		this.title = title;
		this.date = date;
		this.id = id;
	}

	/**
	 * Makes view visible on device
	 *
	 * @param savedInstanceState  If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_img_data);
	}

	/**
	 * Retrieves string value of image url
	 *
	 * @return String value of url of image
	 */
	protected String getImageUrl() {
		return imageUrl;
	}

	/**
	 * Retrieves string value of image description
	 *
	 * @return String value of image description
	 */
	protected String getDescription() {
		return description;
	}

	/**
	 * Retrieves string value of date image was selected as nasa image of the day
	 *
	 * @return String value of date current image was selected as nasa image of the day
	 */
	protected String getDate() {
		return date;
	}

	/**
	 * Retrieves Title of image
	 *
	 * @return Sting title of image
	 */
	protected String getImageTitle() {
		return title;
	}

	/**
	 * Retrieves id of row in database
	 *
	 * @return long value of row id
	 */
	protected long getId() {
		return id;
	}
}
