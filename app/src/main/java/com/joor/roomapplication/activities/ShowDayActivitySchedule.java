package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.joor.roomapplication.R;

public class ShowDayActivitySchedule extends AppCompatActivity {

    public static String INTENT_MESSAGE_KEY = "ROOM_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_day_schedule);
    }
}
