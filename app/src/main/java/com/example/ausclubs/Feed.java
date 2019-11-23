package com.example.ausclubs;

public class Feed {

    /*
    Model class to hold values for each Feed
    This class only consists of Setters and Getters for each feed
     */

    private String Title, clubName, eventDescription, Date;
    private  Double locationLatitude, locationLongitude;
    private String setBy;
    private boolean Hidden;
    private int feedCount = 0;
    private int ID =0;


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public boolean isHidden() {
        return Hidden;
    }

    public void setHidden(boolean hidden) {
        Hidden = hidden;
    }

    public int getFeedCount(){
        return feedCount;
    }

    public void setFeedCount(int feedCount) {
        this.feedCount = feedCount;
    }

    public String getSetBy() {
        return setBy;
    }

    public void setSetBy(String setBy) {
        this.setBy = setBy;
    }

    public String getTitle() {
        return Title;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
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

    public Feed(String title, String clubName, String eventDescription, String date, Double locationLatitude, Double locationLongitude, String setBy) {
        Title = title;
        this.clubName = clubName;
        this.eventDescription = eventDescription;
        Date = date;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.setBy = setBy;
    }

    public Feed () {
        Title = "";
        clubName = "";
        eventDescription = "";
        Date = "";
        locationLatitude = 0.0;
        locationLongitude = 0.0;
        setBy = "";
    }
}
