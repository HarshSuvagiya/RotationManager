package com.scorpion.rotationmanager;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scorpion.rotationmanager.receivers.AdHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public AppAdapter appAdapter;
    public ArrayList<RotationModel> appList = new ArrayList<>();
    public Context context;
    private EditText edtSearch;
    public PickAppListener listener;
    String key = "brightnessManagerList";
    SharedPreferences shref;
    SharedPreferences.Editor editor;
    private RecyclerView recycler;
    public static ArrayList<RotationModel> brightnessManagerList = new ArrayList<>();
    ToggleButton usagePermissionToggle, modifyToggle;
    Button closeDialog;
    CardView viewSave;
    Dialog dialog;
    boolean usageFlag = false, modifyFlag = false;
    EditText editSearch;
    ToggleButton service;
    MyService myService;
    Dialog defaultDialog;
    boolean isCalibrated = false;
    float curBrightnessValue1 = 150;
    ProgressDialog progressDialog;
    RemoteMessage remoteMessage;
    int defaultMode;
    DatabaseReference rootRef;
    private AdView mAdView;
    private AdView mAdView2, mAdView1;
    LinearLayout layBanner2, layBanner1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        FirebaseApp.initializeApp(MainActivity.this);

        try {
            if (getIntent().getExtras() != null) {
                Object value = getIntent().getExtras().get("link");
//                Log.e("MainActivity: ", String.valueOf(value));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) value));
                startActivity(browserIntent);
            }
        } catch (Exception e) {
        }

        findViewById(R.id.privacyPolicy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/rotationmanager/home"));
                startActivity(browserIntent);
            }
        });

        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("RotationManager").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean value = Boolean.parseBoolean(dataSnapshot.child("LoadAd").getValue().toString());

                Utils.AdPos = Integer.parseInt(dataSnapshot.child("AdPerItem").getValue().toString());
                Utils.timesInterAd = Integer.parseInt(dataSnapshot.child("TimesInterAd").getValue().toString());
                Utils.idLoad = value;
                if (Utils.idLoad) {
                    mAdView1 = dialog.findViewById(R.id.adView1);
                    layBanner1 = dialog.findViewById(R.id.banner_container1);
                    AdHelper.AdLoadHelper(context, mAdView1);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            }
//        },2000);

        shref = getSharedPreferences("MyPref", 0);
        editor = shref.edit();
        context = this;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        editSearch = findViewById(R.id.editSearch);
        service = findViewById(R.id.serviceToggle);

//        service.setChecked(true);
//        startService(new Intent(getApplicationContext(), MyService.class));
//        editor.putInt("serviceON", 1);
//        editor.commit();

        if (shref.getInt("serviceON", 0) == 1){
            service.setChecked(true);
            Utils.isServiceOn = 1;
        }
        else{
            service.setChecked(false);
            Utils.isServiceOn = 0;
        }


        service.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(new Intent(getApplicationContext(), MyService.class));
                    editor.putInt("serviceON", 1);
                    Utils.isServiceOn = 1;
                    editor.commit();
                } else {
                    stopService(new Intent(getApplicationContext(), MyService.class));
                    editor.putInt("serviceON", 0);
                    Utils.isServiceOn = 0;
                    editor.commit();
                }
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchApp(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.permission_popup);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        usagePermissionToggle = dialog.findViewById(R.id.chkUsagePermission);
        modifyToggle = dialog.findViewById(R.id.chkModifySettingPermission);
        viewSave = dialog.findViewById(R.id.viewSave);

        usagePermissionToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!checkUsageAccess()) {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
//                    usagePermissionToggle.setChecked(true);
                }
//                else
//                    usagePermissionToggle.setChecked(false);
            }
        });

        modifyToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkSystemWritePermission();
//                if(checkSystemWritePermission())
//                    modifyToggle.setChecked(true);
//                else
//                    modifyToggle.setChecked(false);
            }
        });

        viewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usagePermissionToggle.isChecked() && modifyToggle.isChecked())
                    dialog.dismiss();
                else
                    Toast.makeText(context, "Please allow all permission!!!", Toast.LENGTH_SHORT).show();
            }
        });

        if (!isUsageEnabled(getApplicationContext()) || !isModificationEnabled(getApplicationContext())) {
            dialog.show();
        }

        recycler = findViewById(R.id.recycler);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL);
        recycler.addItemDecoration(new SimpleDividerItemDecoration(this));
        shref = getSharedPreferences("MyFavImages", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String response = shref.getString(key, "");

        if (gson.fromJson(response, new TypeToken<List<RotationModel>>() {
        }.getType()) != null)
            brightnessManagerList = gson.fromJson(response, new TypeToken<List<RotationModel>>() {
            }.getType());
        context = this;

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
//                showDefaultDialog();
            }
        });


//        for(int i = 0 ; i < brightnessManagerList.size() ; i++)
//            Log.e("PACKAGE000",brightnessManagerList.get(i).getPkgName());
        updateRecyclerView("");
    }

    public void addToList() {
        Gson gson = new Gson();
        String json = gson.toJson(brightnessManagerList);
        editor = shref.edit();
        editor.putString(key, json);
        editor.commit();
    }

    public boolean checkUsageAccess() {
        try {
            usageFlag = true;
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
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

    public void searchApp(String appName){
        ArrayList<RotationModel> filterList = new ArrayList<>();

        for (RotationModel model : appList){
            if (model.getAppLabel().toLowerCase().contains(appName.toLowerCase())){
                filterList.add(model);
            }
        }
        appAdapter = new AppAdapter(MainActivity.this, filterList);
        recycler.setAdapter(appAdapter);
    }

    @SuppressLint({"StaticFieldLeak"})
    public void updateRecyclerView(final String str1) {

        new AsyncTask<Void, Void, ArrayList<RotationModel>>() {
            public void onPreExecute() {
                super.onPreExecute();
                appList.clear();
            }

            public ArrayList<RotationModel> doInBackground(Void... voidArr) {
                PackageManager packageManager = context.getPackageManager();
                ArrayList<RotationModel> arrayList = new ArrayList<>();
                Intent intent = new Intent("android.intent.action.MAIN", null);
                intent.addCategory("android.intent.category.LAUNCHER");
                List queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
                for (int i = 0; i < queryIntentActivities.size(); i++) {
                    String str = ((ResolveInfo) queryIntentActivities.get(i)).activityInfo.applicationInfo.packageName;
                    String charSequence = ((ResolveInfo) queryIntentActivities.get(i)).activityInfo.applicationInfo.loadLabel(packageManager).toString();
                    StringBuilder sb = new StringBuilder();
                    sb.append("android.resource://");
                    sb.append(str);
                    sb.append("/");
                    sb.append(((ResolveInfo) queryIntentActivities.get(i)).activityInfo.applicationInfo.icon);
                    String sb2 = sb.toString();

                    if (str1.equals("")) {
                        RotationModel appObject = new RotationModel(charSequence, str, sb2, false, 0);
                        if (!checkAppExists(str, arrayList)) {
                            arrayList.add(appObject);
                        }
                    } else if (charSequence.toLowerCase().contains(str1.toLowerCase())) {
                        RotationModel appObject = new RotationModel(charSequence, str, sb2, false, 0);
                        if (!checkAppExists(str, arrayList)) {
                            arrayList.add(appObject);
                        }
                    }
                }

                Collections.sort(arrayList, new Comparator<RotationModel>() {
                    public int compare(RotationModel appObject, RotationModel appObject2) {
                        return appObject.getAppLabel().compareTo(appObject2.getAppLabel());
                    }
                });
                return arrayList;
            }

            public void onPostExecute(ArrayList<RotationModel> arrayList) {
                super.onPostExecute(arrayList);
                appList.addAll(arrayList);
//                appAdapter.notifyDataSetChanged();
//                pbLoading.setVisibility(8);
                progressDialog.dismiss();
//                ArrayList<BrightnessModel> alist = new ArrayList<>();
//                for (int i = 0; i < appList.size(); i++) {
//                    if (i % Utils.AdPos == 0) {
//                        alist.add(appList.get(i));
//                    }
//                    alist.add(appList.get(i));
//                }
                appAdapter = new AppAdapter(MainActivity.this, appList);
                recycler.setAdapter(appAdapter);

            }
        }.execute(new Void[0]);
    }

    public boolean checkAppExists(String str, ArrayList<RotationModel> arrayList) {
        if (arrayList.size() == 0) {
            return false;
        }
        for (int i = 0; i < arrayList.size(); i++) {
            if (((RotationModel) arrayList.get(i)).getPkgName().equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkSystemWritePermission() {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this);
            if (retVal) {
//                modifyFlag = true;
//                modifyToggle.setChecked(true);

                //Toast.makeText(this, "Write allowed :-) ", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "Write not allowed :-( ", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
        return retVal;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Utils.idLoad)
            initAdmobInter();
        if (!isUsageEnabled(getApplicationContext()))
            usagePermissionToggle.setChecked(false);
        else
            usagePermissionToggle.setChecked(true);

        if (!isModificationEnabled(getApplicationContext()))
            modifyToggle.setChecked(false);
        else
            modifyToggle.setChecked(true);

    }

    public static boolean isUsageEnabled(Context context) {
        boolean z = true;
        if (Build.VERSION.SDK_INT < 21) {
            return true;
        }
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService("appops");
            String str = "android:get_usage_stats";
            if ((Build.VERSION.SDK_INT >= 29 ? appOpsManager.unsafeCheckOpNoThrow(str, applicationInfo.uid, applicationInfo.packageName) : appOpsManager.checkOpNoThrow(str, applicationInfo.uid, applicationInfo.packageName)) != 0) {
                z = false;
            }
            return z;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public static boolean isModificationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            return Settings.System.canWrite(context);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showAdmobInter();
    }

    private InterstitialAd mInterstitialAd;
    public void showAdmobInter() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.e("TAG Admob", "The interstitial loaded.");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    Log.e("TAG Admob", "The interstitial wasn't loaded yet :" + errorCode);
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when the ad is displayed.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                    startActivity(new Intent(MainActivity.this, ExitActivity.class));
                }
            });

        } else {
            startActivity(new Intent(MainActivity.this, ExitActivity.class));
        }
    }

    public void initAdmobInter() {
        mInterstitialAd = new com.google.android.gms.ads.InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.e("TAG Admob", "The interstitial loaded.");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e("TAG Admob", "The interstitial wasn't loaded yet :" + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
            }
        });
    }
}
