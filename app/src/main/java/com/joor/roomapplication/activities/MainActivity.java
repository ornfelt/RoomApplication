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

    //launches activity ShowDay
    private void navigateToShowDayActivity(String room_name){
        Intent intent = new Intent(getApplicationContext(),
                ShowDayActivity.class);
        intent.putExtra(ShowDayActivity.INTENT_MESSAGE_KEY, room_name);
        startActivity(intent);
    }
}
