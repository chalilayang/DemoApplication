package com.example.mi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.example.mi.demoapplication.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApkInfos {
    Context mContext;

    public ApkInfos(Context context){
        mContext = context;
    }

    public List<String> getAllInstalledApkInfo(){
        List<String> apkPackageName = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
        List<ResolveInfo> resolveInfoList = mContext.getPackageManager().queryIntentActivities(intent,0);
        for(ResolveInfo resolveInfo : resolveInfoList){
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (!isSystemPackage(resolveInfo)){
                apkPackageName.add(activityInfo.applicationInfo.packageName);
            }
        }
        return apkPackageName;
    }

    public boolean isSystemPackage(ResolveInfo resolveInfo){
        return ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public Drawable getAppIconByPackageName(String ApkTempPackageName){
        Drawable drawable;
        try{
            drawable = mContext.getPackageManager().getApplicationIcon(ApkTempPackageName);
        }
        catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            drawable = ContextCompat.getDrawable(mContext, R.mipmap.ic_launcher);
        }
        return drawable;
    }

    public String getAppName(String ApkPackageName){
        String Name = "";
        ApplicationInfo applicationInfo;
        PackageManager packageManager = mContext.getPackageManager();
        try {
            applicationInfo = packageManager.getApplicationInfo(ApkPackageName, 0);
            if(applicationInfo!=null){
                Name = (String)packageManager.getApplicationLabel(applicationInfo);
            }
        }catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }
        return Name;
    }

    private static final String TAG = "ApkInfos";
    public static Drawable ddd(Context context, String ApkPackageName, String component) {
        LauncherApps mLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        List<UserHandle> users = mUserManager.getUserProfiles();
        List<UserHandle> profiles = users == null ? Collections.<UserHandle>emptyList() : users;
        Drawable drawable = null;
        for (UserHandle user : profiles) {
            final List<LauncherActivityInfo> apps = mLauncherApps.getActivityList(ApkPackageName, user);
            for (LauncherActivityInfo app : apps) {
                String name = app.getComponentName().getClassName();
                if (component.equals(name)) {
                    return app.getIcon(0);
                }
            }
        }
        return drawable;
    }
}
