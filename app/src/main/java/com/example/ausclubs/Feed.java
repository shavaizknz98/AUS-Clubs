package com.example.ausclubs;

import java.util.ArrayList;

public class Feed {

    private String Title, clubName, eventDescription, Date;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public Feed(String title, String clubName, String eventDescription, String date) {
        Title = title;
        this.clubName = clubName;
        this.eventDescription = eventDescription;
        Date = date;
    }

    public Feed () {
        Title = "";
        clubName = "";
        eventDescription = "";
        Date = "";
    }
}
