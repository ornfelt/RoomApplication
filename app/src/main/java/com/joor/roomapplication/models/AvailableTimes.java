package com.joor.roomapplication.models;

public class AvailableTimes {

    public AvailableTimes(String room, String startTime, String endTime) {
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private String room;
    private String startTime;
    private String endTime;

}
