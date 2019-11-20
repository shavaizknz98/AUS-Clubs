package com.example.ausclubs;

import java.util.ArrayList;

public class Feeds {

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
    }

    public ArrayList<Feed> getFeedList(){
        return feedList;
    }
}
