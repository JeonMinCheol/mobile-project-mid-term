package com.example.mobilewebproject2;

import java.time.LocalDateTime;

public class DataModel {
    private int id;
    private String title;
    private String text;
    private String imageUrl;
    private String publised_date;

    public DataModel(int id, String title, String text, String imageUrl, String publised_date) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;
        this.publised_date = publised_date;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPublised_date() {
        return publised_date;
    }

    public void setPublised_date(String publised_date) {
        this.publised_date = publised_date;
    }

    @Override
    public String toString() {
        return "DataModel{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", publised_date=" + publised_date +
                '}';
    }
}