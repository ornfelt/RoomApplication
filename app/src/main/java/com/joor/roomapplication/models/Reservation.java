package com.joor.roomapplication.models;

import java.util.Arrays;

public class Reservation {

    private int id;
    private String starttime;
    private String startdate;
    private String endtime;
    private String enddate;
    private String[] columns;
    //name variable is set manually
    private String[] name;
    //boolean that reveals if the reservation contains only one room or not
    private boolean soloRoom = false;

    public Reservation(int id, String starttime, String startdate, String endtime, String enddate, String[] columns) {
        super();
        this.id = id;
        this.starttime = starttime;
        this.startdate = startdate;
        this.endtime = endtime;
        this.enddate = enddate;
        this.columns = columns;
    }

    //no args constructor
    public Reservation() {}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getStartTime() {
        return starttime;
    }
    public void setStartTime(String startTime) {
        this.starttime = startTime;
    }
    public String getStartDate() {
        return startdate;
    }
    public void setStartDate(String startDate) {
        this.startdate = startDate;
    }
    public String getEndTime() {
        return endtime;
    }
    public void setEndTime(String endTime) {
        this.endtime = endTime;
    }
    public String getEndDate() {
        return enddate;
    }
    public void setEndDate(String endDate) {
        this.enddate = endDate;
    }
    public String[] getColumns() {
        return columns;
    }
    public void setColumns(String[] columns) {
        this.columns = columns;
    }
    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    //toString method
    @Override
    public String toString() {
        return "Room [id=" + id + ", startTime=" + starttime + ", startDate=" + startdate + ", endTime=" + endtime
                + ", endDate=" + enddate + ", columns=" + Arrays.toString(columns) + ", name="+ Arrays.toString(name) + ", soloRoom=" + soloRoom + "]";
    }
}
