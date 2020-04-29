package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.joor.roomapplication.R;

public class BookingActivity extends AppCompatActivity {

    public static String INTENT_MESSAGE_KEY = "BOOKING_TIME";
    private String booking_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        //get and set chosen booking time
        Intent intent = getIntent();
        booking_time = intent.getStringExtra(INTENT_MESSAGE_KEY);
        TextView textTime = (TextView) findViewById(R.id.textBookingTime);
        textTime.setText(booking_time);
    }
}
