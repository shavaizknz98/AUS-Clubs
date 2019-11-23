package com.example.ausclubs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class firebaseService extends Service {

    /*
    This class is the service for the application that will provide the user with a notification every time a feed has been added.
     */

    private DatabaseReference mFeedCount;
    private boolean firstTime = true;//No notification on the firsttime notification since otherwise a notification will be sent every time the listener is set

    @Override
    public void onCreate() {
        super.onCreate();
        startNotificationListener();//Start Firebase Async Listener for notifications, no need for Timer Tasks since Firebase will automatically call the listener for notifications
    }


    public void startNotificationListener() {
        Log.d("HELLOHELLO", "firebaseService: Service Started");
        if(FirebaseAuth.getInstance().getCurrentUser() != null){//Only provide notification if user is logged in
            mFeedCount = FirebaseDatabase.getInstance().getReference().child("feedCount"); //Set FeedCount value
            mFeedCount.addValueEventListener(new ValueEventListener() {//Set listener for feedCOunt, so if feedCount has changed, then provide notification
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //Boiler plate notification code from Google Notification Documentation
                    createNotificationChannel();

                    // Create an explicit intent for an Activity in your app
                    Intent intent = new Intent(firebaseService.this, feedsActivity.class);

                    PendingIntent pendingIntent = PendingIntent.getActivity(firebaseService.this, 0, intent, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(firebaseService.this, "Event Notifications")
                            .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                            .setContentTitle("New Event From AUS Clubs" )
                            .setContentText("Click here to see updated feeds")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(firebaseService.this);

                    // notificationId is a unique int for each notification that you must define
                    if(!firstTime)//First Time Notifications are to be sent are only after the app has launched AND the user has logged in, This boolean check will take care of that
                        notificationManager.notify(50, builder.build());
                    firstTime = false;

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void createNotificationChannel() {//From Google Notification Documentation
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Notifications";
            String description = "Notification for delivering updates on new events.";
            NotificationChannel channel = new NotificationChannel("Event Notifications", name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mFeedCount = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startNotificationListener();
        return START_STICKY;//Start Service again once resources are available again, if closed due to low memory.
    }


}
