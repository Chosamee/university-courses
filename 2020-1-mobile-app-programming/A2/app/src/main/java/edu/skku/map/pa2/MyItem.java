package edu.skku.map.pa2;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class MyItem {
    private Bitmap icon;
    private String user;
    private String tags;
    private String contents;
    private Bitmap contents_pic;
    private String picLink;
    private String iconLink;

    public void setIconLink(String iconLink){
        this.iconLink = iconLink;
    }
    public String getPicLink(){
        return this.picLink;
    }
    public String getIconLink(){
        return this.iconLink;
    }
    public void setPicLink(String picLink){
        this.picLink = picLink;
    }
    public Bitmap getIcon() {
        return this.icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String gettags() {
        return this.tags;
    }

    public void settags(String tags) {
        this.tags = tags;
    }

    public String getcontents() {
        return this.contents;
    }

    public void setcontents(String contents) {
        this.contents = contents;
    }

    public String getusername() {
        return this.user;
    }

    public void setusername(String user) {
        this.user = user;
    }

    public Bitmap getpic() {
        return this.contents_pic;
    }

    public void setpic(Bitmap contents_pic) {
        this.contents_pic = contents_pic;
    }

}
