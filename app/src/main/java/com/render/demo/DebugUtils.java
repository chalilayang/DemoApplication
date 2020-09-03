package com.render.demo;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by chalilayang on 20-9-3 上午10:17.
 **/
public class DebugUtils {
    private static final String TAG = "DebugUtils";

    public static void saveBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "saveBitmap: bitmap null");
            return;
        }
        Log.i(TAG, "saveBitmap: " + bitmap.getWidth() + " " + bitmap.getHeight());
        File saveFile = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + "DCIM" + File.separator + "debugGL" + File.separator+ UUID.randomUUID() + ".png");
        if (saveFile.exists()) {
            saveFile.delete();
        }
        try (OutputStream outputStream = new FileOutputStream(saveFile);) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
