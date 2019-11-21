package com.example.ausclubs;

public class Users {

    private String Email;
    private String Name;
    private int feedCount;
    private boolean Admin;
    private String clubOne;
    private String clubTwo = "";

    public static String C1 = "";
    public static String C2 = "";

    public Users() {
    }

    public Users(String email, String name, boolean Admin, int feedCount, String clubOne, String clubTwo) {
        Email = email;
        Name = name;
        this.Admin = Admin;
        this.feedCount = feedCount;
        this.clubOne = clubOne;
        this.clubTwo = clubTwo;
    }

    public String getEmail() {
        return Email;
    }

    public String getClubOne() {
        return clubOne;
    }

    public String getClubTwo() {
        return clubTwo;
    }

    public void setClubOne(String clubOne) {
        this.clubOne = clubOne;
        C1 = clubOne;
    }

    public void setClubTwo(String clubTwo) {
        this.clubTwo = clubTwo;
        C2 = clubTwo;
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
