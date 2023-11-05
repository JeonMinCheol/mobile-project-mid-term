package com.example.mobilewebproject2;

import android.media.Image;

public class PostDTO {
    private String location;
    private String text;
    private Image image;

    public PostDTO(String location, String text, Image image) {
        this.location = location;
        this.text = text;
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
