package com.example.ausclubs;

import java.util.ArrayList;

public class Feeds {
        /*
    Model class to hold values for a list of Feeds
    This class only consists of Setters and Getters for each feed
     */

    public static ArrayList<Feed> feedList;

    public Feeds(ArrayList<Feed> feedList) {
        this.feedList = feedList;
    }

    public Feeds () {
        feedList = new ArrayList<>();
    }

    public void addToFeedList (Feed feed){
        feedList.add(feed);
    }

    public void emptyFeed () {
        feedList.clear();
    } //Empty out feedList

    public ArrayList<Feed> getFeedList(){
        return feedList;
    }//Retrieve feed list

    public static void setFeedList(ArrayList<Feed> feedList) { //Change current feed list to another one
        Feeds.feedList = feedList;
    }
}
