package com.mgodevelopment.currencyapp.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by Martin on 9/14/2016.
 */
public class NotificationUtils {

    public static void showNotificationMessage(Context context, String title, String message) {

        if (TextUtils.isEmpty(message)) {
            return;
        }

        if (isAppInBackground(context)) {

        }

    }

    public static boolean isAppInBackground(Context context) {

        boolean isInBackground = true;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {

            List<ActivityManager.RunningAppProcessInfo> runningProcess =
                    activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcess) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess: processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }

        } else {

            List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
            ComponentName componentName = taskInfo.get(0).topActivity;
            if (componentName.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }

        }

        return isInBackground;

    }

}
