package com.example.finalproject;

import java.util.Objects;

public class Article {

    long id;
    String title;
    String description;
    String date;
    String url;
    boolean fav;

    public Article(long id, String title, String description, String date, String url, boolean fav) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.url = url;
        this.fav = fav;
    }

    public boolean isFav() {
        return fav;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return getId() == article.getId() &&
                isFav() == article.isFav() &&
                Objects.equals(getTitle(), article.getTitle()) &&
                Objects.equals(getDescription(), article.getDescription()) &&
                Objects.equals(getDate(), article.getDate()) &&
                Objects.equals(getUrl(), article.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getDescription(), getDate(), getUrl(), isFav());
    }
}
