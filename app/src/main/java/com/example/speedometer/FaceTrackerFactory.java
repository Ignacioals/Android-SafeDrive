package com.example.speedometer;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

public class FaceTrackerFactory implements MultiProcessor.Factory<Face> {

    FaceTrackerFactory() {

    }

    @Override
    public Tracker<Face> create(Face face) {
        return new EyesTracker();
    }
}