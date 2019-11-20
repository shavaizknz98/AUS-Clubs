package com.example.ausclubs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class firebaseService extends Service {

    private DatabaseReference mFeed;

    public firebaseService() {
        mFeed = FirebaseDatabase.getInstance().getReference().child("Users");
        mFeed.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Feed tempFeed = new Feed();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    long feedCount = (long) ds.child("feedCount").getValue();
                    tempFeed = new Feed();
                    if(feedCount!=0) {
                        tempFeed.setTitle(ds.child("Feeds").child("Feed" + feedCount).getValue(Feed.class).getTitle());
                        tempFeed.setDate(ds.child("Feeds").child("Feed" + feedCount).getValue(Feed.class).getDate());
                        tempFeed.setClubName(ds.child("Feeds").child("Feed" + feedCount).getValue(Feed.class).getClubName());
                        tempFeed.setEventDescription(ds.child("Feeds").child("Feed" + feedCount).getValue(Feed.class).getEventDescription());
                    }

                }
                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(firebaseService.this, "Event Notifications")
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                        .setContentTitle("New Event From " + tempFeed.getClubName())
                        .setContentText(tempFeed.getTitle())
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(tempFeed.getEventDescription()))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                // Create an explicit intent for an Activity in your app
                Intent intent = new Intent(firebaseService.this, feedsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(firebaseService.this, 0, intent, 0);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(firebaseService.this);

// notificationId is a unique int for each notification that you must define
                notificationManager.notify(50, builder.build());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void createNotificationChannel() {
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
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");


    }
}
