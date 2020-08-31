package com.joor.roomapplication.utility;

//this singleton class provides utility by counting scripts used used by activity: BookingActivity
public class ScriptCounter {

    //instance of ScriptCounter (singleton)
    private static ScriptCounter single_instance = null;

    //list that contains values
    public int scriptCounter;
    public boolean bookingScriptStarted;

    private ScriptCounter(){
        //init variables
        scriptCounter = 0;
        bookingScriptStarted = false;
    }

    public static ScriptCounter getInstance(){
        if(single_instance == null){
            single_instance = new ScriptCounter();
        }
        return single_instance;
    }

    public void resetScriptCounter(){
        scriptCounter = 0;
        bookingScriptStarted = false;
    }
}
