package com.example.chatme.manipulation;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapHandler {

    public static float HALF_ROTATION_RIGHT = 90f;
    public static Bitmap rotateBitmap(Bitmap originalBitmap, float degreesToRotate) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degreesToRotate);

        return Bitmap.createBitmap(originalBitmap, 0, 0,
                                    originalBitmap.getWidth(),
                                    originalBitmap.getHeight(),
                                    matrix,
                                    true);
    }
}
