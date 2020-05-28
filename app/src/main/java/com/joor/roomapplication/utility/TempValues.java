package com.joor.roomapplication.utility;

import java.util.ArrayList;
import java.util.List;

//this singleton class provides utility by storing values used by activity: DayActivitySchedule. The class is similar to ShowAmountValues, but stores strings instead.
public class TempValues {

    //instance of TempValues (singleton)
    private static TempValues single_instance = null;

    //list that contains values
    public ArrayList<String> tempValuesList;

    private TempValues(){
        //init list
        tempValuesList = new ArrayList<>();
    }

    public static TempValues getInstance(){
        if(single_instance == null){
            single_instance = new TempValues();
        }
        return single_instance;
    }

    public void resetTempValuesList(){
        tempValuesList = new ArrayList<>();
    }
}
