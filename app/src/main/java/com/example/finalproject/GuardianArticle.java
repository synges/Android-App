package com.example.finalproject;

import android.os.Parcel;
import android.os.Parcelable;

public class GuardianArticle implements Parcelable {

    /**
     * The title of this GuardianArticle
     */
    private String title;

    /**
     * The url of this GuardianArticle
     */
    private String url;

    /**
     * The section name of this GuardianArticle
     */
    private String sectionName;

    /**
     * The database id of this GuardianArticle
     */
    private long id;

    /**
     *
     * Creates this GuardianArticle object and initializes
     * all private fields
     *
     * @param title
     * @param url
     * @param sectionName
     * @param id
     */
    public GuardianArticle(String title, String url, String sectionName, long id){
        this.setTitle(title);
        this.setURL(url);
        this.setSectionName(sectionName);
        this.setId(id);
    }

    protected GuardianArticle(Parcel in) {
        title = in.readString();
        url = in.readString();
        sectionName = in.readString();
        id = in.readLong();
    }

    public static final Creator<GuardianArticle> CREATOR = new Creator<GuardianArticle>() {
        @Override
        public GuardianArticle createFromParcel(Parcel in) {
            return new GuardianArticle(in);
        }

        @Override
        public GuardianArticle[] newArray(int size) {
            return new GuardianArticle[size];
        }
    };

    /**
     *
     * @return
     * The title of this GuardianArticle
     */
    public String getTitle(){
        return this.title;
    }

    /**
     *
     * Sets the title of this GuardianArticle
     *
     * @param title
     * The title to be set
     */
    public void setTitle(String title){
        this.title = title;
    }

    /**
     *
     * @return
     * The url of this GuardianArticle
     */
    public String getURL(){
        return this.url;
    }

    /**
     *
     * Sets the url of this GuardianArticle
     *
     * @param url
     * The url to be set
     */
    public void setURL(String url){
        this.url = url;
    }

    /**
     *
     * @return
     * The section name of this GuardianArticle
     */
    public String getSectionName(){
        return this.sectionName;
    }


    /**
     * Sets the section name of this GuardianArticle
     *
     * @param sectionName
     * The section name to be set
     */
    public void setSectionName(String sectionName){
        this.sectionName = sectionName;
    }

    /**
     *
     * @return
     * The id of this GuardianArticle
     */
    public long getId(){
        return this.id;
    }

    /**
     * Sets the id of this GuardianArticle
     *
     * @param id
     * The id to be set
     */
    public void setId(long id){
        this.id = id;
    }

    /**
     *
     * @return
     * A String containing each of this GuardianArticle's fields
     */
    public String toString(){
        return this.title + ", URL: " + this.getURL() + ", Section Name:: " + this.getSectionName() + ", ID: " + this.getId();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(sectionName);
        dest.writeLong(id);
    }
}
