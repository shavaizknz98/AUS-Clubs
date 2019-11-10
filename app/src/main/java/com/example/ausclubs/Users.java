package com.example.ausclubs;

public class Users {

    private String Email;
    private String Name;
    private int feedCount;
    private boolean Admin;

    public Users() {
    }

    public Users(String email, String name, boolean Admin, int feedCount) {
        Email = email;
        Name = name;
        this.Admin = Admin;
        this.feedCount = feedCount;
    }

    public String getEmail() {
        return Email;
    }

    public int getFeedCount() {return feedCount;}


    public void setEmail(String email) {
        Email = email;
    }
    public void setFeedCount(int feedCount) { this.feedCount = feedCount;}

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean isAdmin() {
        return Admin;
    }


    public void setAdmin(boolean Admin) {
        this.Admin = Admin;
    }


}
