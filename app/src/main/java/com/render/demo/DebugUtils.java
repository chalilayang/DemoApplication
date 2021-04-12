package com.render.demo;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.ReflectUtil;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
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

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                Class classClz = Class.class;
                Field classLoaderField = classClz.getDeclaredField("classLoader");
                classLoaderField.setAccessible(true);
                classLoaderField.set(ReflectUtil.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object updateDisplayList(View view) {
        Object renderNode = null;
        try {
            renderNode = ReflectUtil.getObjectField(
                    view, View.class, "updateDisplayListIfDirty");
        } catch (Exception e) {
            Log.e(TAG, "_updateDisplayList: " + e);
            e.printStackTrace();
        }
        return renderNode;
    }

    public static Object getRenderNode(View view) {
        if (view == null) {
            return null;
        }
        Object renderNode = null;
        try {
            renderNode = ReflectUtil.getObjectField(view, View.class, "mRenderNode");
        } catch (Exception e) {
            Log.e(TAG, "getRenderNode: " + e);
            e.printStackTrace();
        }
        return renderNode;
    }

    public static boolean displayListValid(Object renderNode) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            return hasDisplayList(renderNode);
        } else {
            return isValid(renderNode);
        }
    }

    public static boolean hasDisplayList(Object renderNode) {
        if (renderNode == null) {
            return false;
        }
        boolean result = false;
        try {
            result = (Boolean) ReflectUtil.callAnyObjectMethod(
                    renderNode.getClass(),
                    renderNode, "hasDisplayList", new Class[]{}, null);
        } catch (Exception e) {
            Log.e(TAG, "hasDisplayList: " + e);
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isValid(Object renderNode) {
        if (renderNode == null) {
            return false;
        }
        boolean result = false;
        try {
            result = (Boolean) ReflectUtil.callAnyObjectMethod(
                    renderNode.getClass(),
                    renderNode, "isValid", new Class[]{}, null);
        } catch (Exception e) {
            Log.e(TAG, "isValid: " + e);
            e.printStackTrace();
        }
        return result;
    }

    public static Class<?> sRenderNodeClazz = null;
}
