package com.example.ausclubs;

import android.app.Application;
import android.content.Intent;

public class App extends Application {
    @Override
    public void onCreate() {
        Intent service = new Intent(this, firebaseService.class);
        startService(service);
        super.onCreate();
    }
}
