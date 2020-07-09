package com.example.speedometer;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class Report {

    static ArrayList<String> nameArray = new ArrayList<String>();

    static ArrayList<String> infoArray = new ArrayList<String>();

    static ArrayList<String> speedArray = new ArrayList<String>();

    public Report(){
        nameArray.add("23/06/2020 18:26:56 hs");
        nameArray.add("23/06/2020 18:50:26 hs");
        nameArray.add("23/06/2020 19:06:32 hs");
        nameArray.add("24/06/2020 15:26:56 hs");
        nameArray.add("24/06/2020 16:15:53 hs");
        infoArray.add("El conductor utilizó el telefono");
        infoArray.add("El conductor utilizó el telefono");
        infoArray.add("El conductor utilizó el telefono");
        infoArray.add("El conductor utilizó el telefono");
        infoArray.add("El conductor utilizó el telefono");
        speedArray.add("Vel: 56.2 km/hr");
        speedArray.add("Vel: 72.4 km/hr");
        speedArray.add("Vel: 62.0 km/hr");
        speedArray.add("Vel: 42.5 km/hr");
        speedArray.add("Vel: 120.6 km/hr");
    }

    public void addReport(String date, String speed){
        nameArray.add(date + " hs");
        infoArray.add("El conductor utilizó el telefono");
        speedArray.add("Vel: " + speed);
    }

    public static ArrayList<String> getNameArray(){
        return nameArray;
    }

    public static ArrayList<String> getInfoArray(){
        return infoArray;
    }

    public static ArrayList<String> getSpeedArray(){
        return speedArray;
    }
}
