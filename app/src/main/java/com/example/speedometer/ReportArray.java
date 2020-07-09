package com.example.speedometer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ReportArray extends ArrayAdapter {

    //to reference the Activity
    private final Activity context;

    //to store the list of countries
    private final ArrayList<String> nameArray;

    //to store the list of countries
    private final ArrayList<String> infoArray;

    private final ArrayList<String> speedArray;

    public ReportArray(Activity context, ArrayList<String> nameArrayParam, ArrayList<String> infoArrayParam, ArrayList<String> speedArrayParam){

        super(context,R.layout.list_rows , nameArrayParam);
        this.context=context;
        this.nameArray = nameArrayParam;
        this.infoArray = infoArrayParam;
        this.speedArray = speedArrayParam;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_rows, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.nameTextViewID);
        TextView infoTextField = (TextView) rowView.findViewById(R.id.infoTextViewID);
        TextView speedTextField = (TextView) rowView.findViewById(R.id.speedTextViewID);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray.get(position));
        infoTextField.setText(infoArray.get(position));
        speedTextField.setText(speedArray.get(position));

        return rowView;

    };
}
