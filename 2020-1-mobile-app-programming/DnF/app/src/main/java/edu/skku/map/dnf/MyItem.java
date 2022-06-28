package edu.skku.map.dnf;

import android.graphics.Bitmap;

public class MyItem {
    private String username;
    private String title;
    private String contents;
    private Bitmap contents_pic;
    private String picLink;
    private String characterName;
    private String serverName;

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Bitmap getContents_pic() {
        return contents_pic;
    }

    public void setContents_pic(Bitmap contents_pic) {
        this.contents_pic = contents_pic;
    }

    public String getPicLink() {
        return picLink;
    }

    public void setPicLink(String picLink) {
        this.picLink = picLink;
    }
}
