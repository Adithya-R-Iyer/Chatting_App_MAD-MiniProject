package com.example.whatsapp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyAlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM_TRIGGERED = "com.example.myapp.ACTION_ALARM_TRIGGERED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Broadcast the event to the ChatDetailsActivity
        Intent eventIntent = new Intent(ACTION_ALARM_TRIGGERED);
        context.sendBroadcast(eventIntent);
    }
}
