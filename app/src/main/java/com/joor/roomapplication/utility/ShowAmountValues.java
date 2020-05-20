package com.joor.roomapplication.utility;

import java.util.ArrayList;
import java.util.List;

//this singleton class provides utility by storing showAmount-values used by activity: FirstAvailable
public class ShowAmountValues {

    //instance of showAmountList (singleton)
    private static ShowAmountValues single_instance = null;

    //list that contains values
    public ArrayList<Integer> showAmountList;

    private ShowAmountValues(){
        //init list
        showAmountList = new ArrayList<>();
    }

    public static ShowAmountValues getInstance(){
        if(single_instance == null){
            single_instance = new ShowAmountValues();
        }
        return single_instance;
    }

    public void resetShowAmountList(){
        showAmountList = new ArrayList<>();
    }
}
