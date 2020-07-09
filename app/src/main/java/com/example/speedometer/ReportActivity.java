package com.example.speedometer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ReportActivity extends AppCompatActivity {

    static ImageView arrow;
    ArrayList<String> nameArray = new ArrayList<String>();

    ListView listView;

    ArrayList<String> infoArray = new ArrayList<String>();

    ArrayList<String> speedArray = new ArrayList<String>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2_main);

        nameArray = Report.getNameArray();
        infoArray = Report.getInfoArray();
        speedArray = Report.getSpeedArray();

        arrow = (ImageView) findViewById(R.id.backarrow);
        arrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }

        });

        ReportArray whatever = new ReportArray(this, nameArray, infoArray, speedArray);
        listView = (ListView) findViewById(R.id.listdetections);
        listView.setAdapter(whatever);

    }


}
