package com.scorpion.rotationmanager;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class MyService extends Service {

    CountDownTimer countDownTimer;
    public ArrayList<RotationModel> brightnessManagerList = new ArrayList<>();
    String key = "brightnessManagerList";
    SharedPreferences shref;
    SharedPreferences.Editor editor;
    Gson gson = new Gson();
    String response;
    float curBrightnessValue = 50;
    boolean flag = true;
    boolean flag2 = true;
    int defaultOrientationMode;

    @Override
    public void onCreate() {
        super.onCreate();
        onDestroy();
        setForeground();

//        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);  //this will set the automatic mode on
//        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//        Settings.System.putInt(getApplicationContext().getContentResolver(),
//                Settings.System.SCREEN_BRIGHTNESS, 75);


        shref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        defaultOrientationMode = shref.getInt("defaultOrientationMode",1);

//        if (gson.fromJson(response, new TypeToken<List<BrightnessModel>>() {
//        }.getType()) != null)
//            brightnessManagerList = gson.fromJson(response, new TypeToken<List<BrightnessModel>>() {
//            }.getType());


        countDownTimer = new CountDownTimer(10000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();


                response = shref.getString(key, "");
                if (gson.fromJson(response, new TypeToken<List<RotationModel>>() {
                }.getType()) != null)
                    brightnessManagerList = gson.fromJson(response, new TypeToken<List<RotationModel>>() {
                    }.getType());


                if (flag) {
                    try {
                        curBrightnessValue = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                for (int j = 0; j < brightnessManagerList.size(); j++) {
                    if (brightnessManagerList.get(j).getPkgName().equals(getCurrentAppName())) {
                        flag = false;
                        flag2 = true;

                        if (brightnessManagerList.get(j).isAutoRotation()) {
                            setAutoOrientationEnabled(getApplicationContext(),true);
                        } else {
                            if(brightnessManagerList.get(j).getOrientationMode() == 1){
                                Settings.System.putInt(getApplicationContext().getContentResolver(), "accelerometer_rotation", 0);
                                Settings.System.putInt(MyService.this.getContentResolver(), "user_rotation", 0);
                            }
                            else{
                                Settings.System.putInt(getApplicationContext().getContentResolver(), "accelerometer_rotation", 0);
                                Settings.System.putInt(MyService.this.getContentResolver(), "user_rotation", 1);
                            }

//                            AudioManager audioManager;
//                            audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//                            Log.e("WAKELOCK1", String.valueOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)));
//                            Log.e("WAKELOCK2", String.valueOf(audioManager.getStreamVolume(AudioManager.STREAM_RING)));
//                            Log.e("WAKELOCK3", String.valueOf(audioManager.getStreamVolume(AudioManager.STREAM_ALARM)));
//                            Log.e("WAKELOCK4", String.valueOf(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)));
//                            Log.e("WAKELOCK5", String.valueOf(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)));
//                            Log.e("WAKELOCK6", String.valueOf(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)));
//                            Log.e("WAKELOCK", String.valueOf(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)));
//
//                            audioManager.setStreamVolume(AudioManager.STREAM_RING,15 , 0);
                        }
                        break;
                    }
                    else
                        flag = true;
                }
                if (flag) {
//                    flag = true;
                    if(flag2){
                        Settings.System.putInt(getApplicationContext().getContentResolver(), "accelerometer_rotation", 0);
                        Settings.System.putInt(MyService.this.getContentResolver(), "user_rotation", 0);
                        setAutoOrientationEnabled(getApplicationContext(),false);
                        flag = false;
                        flag2 = false;
                    }

//                    if(defaultOrientationMode == 2)
//                        setAutoOrientationEnabled(getApplicationContext(),true);
//                    else
//                        setAutoOrientationEnabled(getApplicationContext(),false);
                }
            }

            @Override
            public void onFinish() {
                countDownTimer.start();
            }
        }.start();

    }

    public static void setAutoOrientationEnabled(Context context, boolean enabled) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
    }

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isForeground(Context ctx, String myPackage) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if (componentInfo.getPackageName().equals(myPackage)) {
            return true;
        }
        return false;
    }

    public String getCurrentAppName() {

        long currentTimeMillis = System.currentTimeMillis();
        List<UsageStats> queryUsageStats = ((UsageStatsManager) getApplicationContext().getSystemService("usagestats")).queryUsageStats(0, currentTimeMillis - 3600000, currentTimeMillis);
        ActivityManager activityManager = (ActivityManager) getSystemService("activity");
        String str = activityManager.getRunningAppProcesses().get(0).processName;
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(5);

        if (Build.VERSION.SDK_INT > 20) {
            if (queryUsageStats != null) {
                TreeMap treeMap = new TreeMap();
                for (UsageStats next : queryUsageStats) {
                    treeMap.put(Long.valueOf(next.getLastTimeUsed()), next);
                }
                if (treeMap.isEmpty()) {
                    str = BuildConfig.FLAVOR;
                } else {
                    UsageStats usageStats = (UsageStats) treeMap.get(treeMap.lastKey());
                    if (usageStats.getTotalTimeInForeground() > 0) {
                        str = usageStats.getPackageName();
                    }
                }
            }
        } else if (runningTasks.size() > 0) {
            str = runningTasks.get(0).topActivity.getPackageName();
        }
        Log.e("PROCESSMANAGER123", str);
        return str;
    }

    public static String getTopAppName(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String strName = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                strName = getLollipopFGAppPackageName(context);
            } else {
                strName = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strName;
    }

    private static String getLollipopFGAppPackageName(Context ctx) {

        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) ctx.getSystemService("usagestats");
            long milliSecs = 60 * 1000;
            Date date = new Date();
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, date.getTime() - milliSecs, date.getTime());
            if (queryUsageStats.size() > 0) {
            }
            long recentTime = 0;
            String recentPkg = "";
            for (int i = 0; i < queryUsageStats.size(); i++) {
                UsageStats stats = queryUsageStats.get(i);
                if (stats.getLastTimeStamp() > recentTime) {
                    recentTime = stats.getLastTimeStamp();
                    recentPkg = stats.getPackageName();
                }
            }
            return recentPkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void setForeground() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            NotificationChannel notificationChannel = new NotificationChannel("noni", "system", 4);
            notificationManager.createNotificationChannel(notificationChannel);
            startForeground(11, new Notification.Builder(this, notificationChannel.getId()).setContentTitle(getResources().getString(R.string.app_name)).setContentText("Long press to hide this notification").setSmallIcon(R.drawable.ic_notifications_black_24dp).build());
            return;
        }
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getResources().getString(R.string.app_name)).setSmallIcon(R.drawable.ic_notifications_black_24dp).setContentText("Long press to hide this notification");
        startForeground(11, builder.build());
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(NotificationCompat.CATEGORY_ALARM);
        long elapsedRealtime = SystemClock.elapsedRealtime() + 10000;
        Intent intent2 = new Intent(this, CallBroadCastReceiver.class);
        intent2.setAction("com.wond.call.coming");
        alarmManager.set(2, elapsedRealtime, PendingIntent.getBroadcast(this, 0, intent2, 0));
        return super.onStartCommand(intent, i, i2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class LocalBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }
}
