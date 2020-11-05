package com.example.mi.screenshot;

import android.graphics.Region;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import android.view.IWindowSession;
import android.view.InputChannel;
import android.view.InputEventReceiver;
import android.view.ReflectUtil;
import android.view.SurfaceControl;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by chalilayang on 20-10-29 下午2:07.
 **/
public class ScreenshotUtil {
    private static final String TAG = "ScreenshotUtil";
    public static ViewRootImpl getViewRootImpl(View view) {
        if (view == null) {
            return null;
        }
        View rootView = view.getRootView();
        if (rootView == null) {
            return null;
        }
        ViewRootImpl result = null;
        WindowManagerGlobal windowManagerGlobal = WindowManagerGlobal.getInstance();
        if (windowManagerGlobal != null) {
            try {
                ArrayList<ViewRootImpl> list
                        = ReflectUtil.getObjectField(windowManagerGlobal, "mRoots", ArrayList.class);
                for (int index = 0, count = list.size(); index < count; index ++) {
                    ViewRootImpl viewRoot = list.get(index);
                    View mView = ReflectUtil.getObjectField(viewRoot, "mView", View.class);
                    Log.i(TAG, "getViewRootImpl: " + mView + "  " + rootView + " " + rootView.equals(mView));
                    if (rootView.equals(mView)) {
                        result = viewRoot;
                        break;
                    }
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static SurfaceControl getSurfaceControl(ViewRootImpl viewRoot) {
        SurfaceControl surfaceControl = null;
        try {
//            Class<? extends Object> clazz = viewRoot.getClass();
//            Method[] declaredFields = clazz.getDeclaredMethods();
//            for (Method field : declaredFields) {
//                Log.i(TAG, "getSurfaceControl: " + field.getName() + " " + field.getModifiers());
//            }
            surfaceControl = (SurfaceControl) ReflectUtil.callObjectMethod(
                    viewRoot, "getSurfaceControl", new Class[]{}, null);
        } catch (Exception e) {
            Log.i(TAG, "getSurfaceControl: " + e.getMessage());
        }
        return surfaceControl;
    }

    public static InputChannel getInputChannel(ViewRootImpl viewRoot) {
        InputChannel inputChannel = null;
        try {
            inputChannel = ReflectUtil.getObjectField(viewRoot, "mInputChannel", InputChannel.class);
        } catch (Exception e) {
            Log.i(TAG, "getInputChannel: " + e.getMessage());
        }
        return inputChannel;
    }

    public static InputEventReceiver getInputEventReceiver(ViewRootImpl viewRoot) {
        InputEventReceiver inputChannel = null;
        try {
            inputChannel = ReflectUtil.getObjectField(
                    viewRoot, "mInputEventReceiver", InputEventReceiver.class);
        } catch (Exception e) {
            Log.i(TAG, "getInputEventReceiver: " + e.getMessage());
        }
        return inputChannel;
    }

    public static IBinder getInputToken(InputEventReceiver viewRoot) {
        IBinder inputChannel = null;
        try {
            inputChannel = (IBinder) ReflectUtil.callObjectMethod2(
                    viewRoot, "getToken", new Class[]{}, null);
        } catch (Exception e) {
            Log.i(TAG, "getInputToken: " + e.getMessage());
        }
        return inputChannel;
    }

    public static IBinder getInputToken(InputChannel viewRoot) {
        IBinder inputChannel = null;
        try {
            inputChannel = (IBinder) ReflectUtil.callObjectMethod2(
                    viewRoot, "getToken", new Class[]{}, null);
        } catch (Exception e) {
            Log.i(TAG, "getInputToken: " + e.getMessage());
        }
        return inputChannel;
    }

    @RequiresApi(api = 30)
    public static boolean updateTouchRegion(View view, Region region, WindowManager.LayoutParams lp, int id) {
        if (SDK_INT > Build.VERSION_CODES.P) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
                Object sVmRuntime = getRuntime.invoke(null);
                setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{new String[]{"L"}});
            } catch (Throwable e) {
                Log.e("[error]", "reflect bootstrap failed:", e);
            }
        }

        ViewRootImpl viewRoot = getViewRootImpl(view);
        if (viewRoot == null) {
            Log.i(TAG, "updateTouchRegion: ViewRootImpl == null");
            return false;
        }
        SurfaceControl surfaceControl = getSurfaceControl(viewRoot);
        if (surfaceControl == null) {
            Log.i(TAG, "updateTouchRegion: surfaceControl == null");
            return false;
        }
        IBinder inputToken = null;
        InputChannel inputChannel = getInputChannel(viewRoot);
        if (inputChannel == null) {
            Log.i(TAG, "updateTouchRegion: inputChannel == null");
            return false;
        }
        inputToken = getInputToken(inputChannel);
        if (inputToken == null) {
            Log.i(TAG, "updateTouchRegion: inputToken == null");
            return false;
        }
        IWindowSession windowSession = WindowManagerGlobal.getWindowSession();
        if (windowSession == null) {
            Log.i(TAG, "updateTouchRegion: windowSession == null");
            return false;
        }
        boolean success = false;
        int flags = lp.flags;
        try {
            ReflectUtil.callObjectMethod(windowSession,
                    "updateInputChannel",
                    new Class[]{IBinder.class, int.class, SurfaceControl.class, int.class, Region.class},
                    inputToken, id, surfaceControl, flags, region);
            success = true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return success;
    }
}
