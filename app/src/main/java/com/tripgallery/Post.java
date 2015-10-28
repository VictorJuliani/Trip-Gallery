package com.tripgallery;

/**
 * Created by matheus on 10/27/15.
 */
public class Post {
    public String url;
    public int likes;
    public String hashtags;
    public String location;

    public Post(String url, int likes, String hashtags, String location) {
        this.url      = url;
        this.likes    = likes;
        this.hashtags = hashtags;
        this.location = location;
    }

}
