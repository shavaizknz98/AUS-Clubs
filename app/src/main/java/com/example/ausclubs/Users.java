package com.example.ausclubs;

public class Users {


    /*

    This is the model class that holds the Users information for when they sign up, it can be used to see which clubs the user is part of when adding
    an event to the feed list

     */

    private String Email;
    private String Name;
    private int feedCount;
    private boolean Admin;
    private String clubOne;
    private String clubTwo = "";

    public static String C1 = "";//Since only one user can be logged in at a time, this will hold a ClubONe and ClubTwo names and can be accessed from any class since it is static
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
        C1 = clubOne;//Set Static Variable for Club Names
    }

    public void setClubTwo(String clubTwo) {
        this.clubTwo = clubTwo;
        C2 = clubTwo;//Set Static Variable for Club Names
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
