package org.example.BlogWebApp.entities;


public class Post {
    public int id;
    public int userId;
    public String title;
    public String body;

    public Post(int i, int i1, String st, String sb) {
        this.id = i;
        this.userId = i1;
        this.title = st;
        this.body = sb;
    }
}
