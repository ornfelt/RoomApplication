package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.joor.roomapplication.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //launches activity ShowItems
    public void onClickShowRooms(View view){
        Intent intent = new Intent(getApplicationContext(),
                ShowReservationsActivity.class);
        startActivity(intent);
    }

    //when user clicks on show day activity
    public void onClickShowDay(View view){
        //gets selected room from user
        EditText roomNameText = (EditText) findViewById(R.id.editTextRoomName);
        String roomName = roomNameText.getText().toString();
        navigateToShowDayActivity(roomName);
    }

    //when user clicks on show day activity (schedule)
    public void onClickShowDaySchedule(View view){
        //gets selected room from user
        EditText roomNameText = (EditText) findViewById(R.id.editTextRoomName);
        String roomName = roomNameText.getText().toString();
        navigateToShowDayActivitySchedule(roomName);
    }

    //when user clicks on show day activity (per hour)
    public void onClickShowDayHours(View view){
        //gets selected room from user
        EditText roomNameText = (EditText) findViewById(R.id.editTextRoomName);
        String roomName = roomNameText.getText().toString();
        navigateToShowDayActivityPerHour(roomName);
    }


    //launches activity ShowDayActivity
    private void navigateToShowDayActivity(String room_name){
        Intent intent = new Intent(getApplicationContext(),
                ShowDayActivity.class);
        intent.putExtra(ShowDayActivity.INTENT_MESSAGE_KEY, room_name);
        startActivity(intent);
    }

    //launches activity ShowDayActivitySchedule
    private void navigateToShowDayActivitySchedule(String room_name){
        Intent intent = new Intent(getApplicationContext(),
                ShowDayActivitySchedule.class);
        intent.putExtra(ShowDayActivitySchedule.INTENT_MESSAGE_KEY, room_name);
        startActivity(intent);
    }

    //launches activity ShowDayActivityPerHour
    private void navigateToShowDayActivityPerHour(String room_name){
        Intent intent = new Intent(getApplicationContext(),
                ShowDayActivityPerHour.class);
        intent.putExtra(ShowDayActivityPerHour.INTENT_MESSAGE_KEY, room_name);
        startActivity(intent);
    }
}
