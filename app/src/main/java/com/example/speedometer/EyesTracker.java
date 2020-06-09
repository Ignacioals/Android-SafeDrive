package com.example.speedometer;

import android.view.View;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

public class EyesTracker extends Tracker<Face> {

    private final float THRESHOLD = 0.7f;

    EyesTracker() {

    }

    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        if(MainActivity.p == 0) {
            if (face.getIsLeftEyeOpenProbability() > THRESHOLD || face.getIsRightEyeOpenProbability() > THRESHOLD) {
                if(!MainActivity.eyeDetected) {
                    MainActivity.eyeDetected = true;
                    MainActivity.onOjos.setVisibility(View.VISIBLE);
                }

            } else {
                MainActivity.eyeDetected = false;
                MainActivity.onOjos.setVisibility(View.INVISIBLE);

            }
        }
    }

    @Override
    public void onMissing(Detector.Detections<Face> detections) {
        super.onMissing(detections);
    }

    @Override
    public void onDone() {
        super.onDone();
    }
}