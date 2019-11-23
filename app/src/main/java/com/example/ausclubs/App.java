package com.example.ausclubs;

import android.app.Application;
import android.content.Intent;

public class App extends Application {//App will launch only once when the application is opened, it is used to launch service
    @Override
    public void onCreate() {
        super.onCreate();

        Intent service = new Intent(this, firebaseService.class);
        startService(service);//Start Service
    }
}
