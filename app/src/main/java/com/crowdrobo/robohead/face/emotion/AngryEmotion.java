package com.crowdrobo.robohead.face.emotion;

import com.crowdrobo.robohead.face.EyeBrow;
import com.crowdrobo.robohead.face.FaceScreen;
import com.crowdrobo.robohead.face.Mouth;

/**
 * Created by rory on 30/01/16.
 */
public class AngryEmotion implements Emotion {
    @Override
    public void applyEmotion(FaceScreen faceScreen) {
        faceScreen.getLeftEyeBrow().setPixelInfo(EyeBrow.LEFT_ANGRY);
        faceScreen.getRightEyeBrow().setPixelInfo(EyeBrow.RIGHT_ANGRY);
        faceScreen.getMouth().setPixelInfo(Mouth.FLAT);
    }
}
