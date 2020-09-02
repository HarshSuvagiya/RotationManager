package com.scorpion.rotationmanager;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scorpion.rotationmanager.receivers.AdHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.scorpion.rotationmanager.MainActivity.brightnessManagerList;

public class AppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<RotationModel> appList;
    private Context context;
    SharedPreferences shref;
    SharedPreferences.Editor editor;
    String key = "brightnessManagerList";
    int k = 0;
    int getOrientationMode = 1;
    boolean isAutoRotationChecked = false;
    float MaxBrightness;
    int additionalContent = 0;

    class ADHolder extends RecyclerView.ViewHolder {
        AdView adView;

        public ADHolder(@NonNull View itemView) {
            super(itemView);
            this.setIsRecyclable(false);
            adView = itemView.findViewById(R.id.adView);
        }
    }

    class ViewHolder1 extends RecyclerView.ViewHolder {
        public ImageView im_icon_app;
        public TextView tv_app_name, tv_app_package;
        ToggleButton toggle;


        ViewHolder1(View view) {
            super(view);
            this.setIsRecyclable(false);
            this.im_icon_app = (ImageView) view.findViewById(R.id.im_icon_app);
            this.tv_app_name = (TextView) view.findViewById(R.id.tv_app_name);
            this.tv_app_package = (TextView) view.findViewById(R.id.tv_app_package);
            this.toggle = view.findViewById(R.id.toggle);

        }
    }

    public AppAdapter(Context context2, ArrayList<RotationModel> arrayList) {
        this.context = context2;
        this.appList = arrayList;

        shref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String response = shref.getString(key, "");

        if (gson.fromJson(response, new TypeToken<List<RotationModel>>() {
        }.getType()) != null)
            brightnessManagerList = gson.fromJson(response, new TypeToken<List<RotationModel>>() {
            }.getType());

        MaxBrightness = shref.getFloat("MaxBrightness", 255);

    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == CONTENT_TYPE) {
            return new ViewHolder1(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_app, viewGroup, false));
        } else {
            return new ADHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_ad, viewGroup, false));
        }
    }

    private int getRealPosition(int position) {
        if (Utils.AdPos == 0) {
            return position;
        } else {
            return position - position / Utils.AdPos;
        }
    }

    private AdView mAdView1;
    LinearLayout viewSeekbarContainer;

    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {

        if (getItemViewType(i) == AD_TYPE) {
            AdHelper.AdLoadHelper(context, ((ADHolder) viewHolder).adView);
        } else {

            final int i1 = getRealPosition(i);
            final ViewHolder1 viewHolderNew = (ViewHolder1) viewHolder;
            Glide.with(context).asBitmap().load(Uri.parse(((RotationModel) appList.get(i1)).getIcon())).thumbnail(0.5f).into(((ViewHolder1) viewHolder).im_icon_app);
            ((ViewHolder1) viewHolder).tv_app_name.setText(appList.get(i1).getAppLabel());
            ((ViewHolder1) viewHolder).tv_app_package.setText(appList.get(i1).getPkgName());

            ((ViewHolder1) viewHolder).toggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MaxBrightness = shref.getFloat("MaxBrightness", 200);
                    if (Utils.isServiceOn == 1) {
                        if (viewHolderNew.toggle.isChecked()) {
                            viewHolderNew.toggle.setChecked(true);
                            final Dialog dialog = new Dialog(context);
                            dialog.setContentView(R.layout.configure_brightness_popup);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.setCancelable(false);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                            if (Utils.idLoad) {

                                mAdView1 = dialog.findViewById(R.id.adView);
                                AdRequest adRequest = new AdRequest.Builder().build();
                                mAdView1.loadAd(adRequest);

                                mAdView1.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdLoaded() {
                                        // Code to be executed when an ad finishes loading.
//                    mAdView.loadAd(adRequest);
                                    }

                                    @Override
                                    public void onAdFailedToLoad(int errorCode) {
                                        // Code to be executed when an ad request fails.
                                    }

                                    @Override
                                    public void onAdOpened() {
                                        // Code to be executed when an ad opens an overlay that
                                        // covers the screen.
                                    }

                                    @Override
                                    public void onAdClicked() {
                                        // Code to be executed when the user clicks on an ad.
                                    }

                                    @Override
                                    public void onAdLeftApplication() {
                                        // Code to be executed when the user has left the app.
                                    }

                                    @Override
                                    public void onAdClosed() {
                                        // Code to be executed when the user is about to return
                                        // to the app after tapping on an ad.
                                    }
                                });
                            }

                            TextView txtAppName;
                            final TextView txtBrightnessLevelPreview;
                            ImageView imgAppIcon;
                            CardView viewSave;
                            ToggleButton chkAutoBrightness;
                            final RadioButton portrait, landscape;
                            LinearLayout landscapell,portraitll;
                            viewSeekbarContainer = dialog.findViewById(R.id.viewSeekbarContainer);

                            txtAppName = dialog.findViewById(R.id.txtAppName);
                            txtBrightnessLevelPreview = dialog.findViewById(R.id.txtBrightnessLevelPreview);
                            imgAppIcon = dialog.findViewById(R.id.imgAppIcon);
                            viewSave = dialog.findViewById(R.id.viewSave);
                            chkAutoBrightness = dialog.findViewById(R.id.chkAutoBrightness);
                            portrait = dialog.findViewById(R.id.rdPortrait);
                            landscape = dialog.findViewById(R.id.rdLandscape);
                            landscapell = dialog.findViewById(R.id.landscapell);
                            portraitll = dialog.findViewById(R.id.portraitll);
                            txtAppName.setText(appList.get(i1).getAppLabel());
                            Picasso.get().load(appList.get(i1).getIcon()).placeholder(R.mipmap.ic_launcher).into(imgAppIcon);

                            landscapell.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    landscape.setChecked(true);
                                    portrait.setChecked(false);
                                    getOrientationMode = 0;
                                }
                            });

                            portraitll.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    landscape.setChecked(false);
                                    portrait.setChecked(true);
                                    getOrientationMode = 1;
                                }
                            });


                            viewSave.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    brightnessManagerList.add(appList.get(i1));
//                                    addToList();
                                    appList.get(i1).setOrientationMode(getOrientationMode);
                                    appList.get(i1).setAutoRotation(isAutoRotationChecked);
                                    dialog.dismiss();
                                    addToList();
                                }
                            });

                            chkAutoBrightness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        isAutoRotationChecked = true;
                                        viewSeekbarContainer.setVisibility(View.GONE);
                                    } else {
                                        isAutoRotationChecked = false;
                                        viewSeekbarContainer.setVisibility(View.VISIBLE);
                                    }

                                }
                            });

                            dialog.show();
                        } else {
                            for (int j = 0; j < brightnessManagerList.size(); j++) {
                                if (brightnessManagerList.get(j).getPkgName().equals(appList.get(i1).getPkgName()))
                                    brightnessManagerList.remove(j);
                            }
                            addToList();
                        }
                    } else {
                        Toast.makeText(context, "Please turn on service", Toast.LENGTH_SHORT).show();
                        viewHolderNew.toggle.setChecked(false);
                    }
                }
            });

            for (int j = 0; j < brightnessManagerList.size(); j++) {
//                Log.e("BRIGHT", brightnessManagerList.get(j).getPkgName() + " " + j);
//                Log.e("BRIGHT1", appList.get(i).getPkgName() + " " + i);
                if (appList.get(i1).getPkgName().equals(brightnessManagerList.get(j).getPkgName()))
                    ((ViewHolder1) viewHolder).toggle.setChecked(true);
//                else if(!brightnessManagerList.contains(appList))
//                    ((ViewHolder1) viewHolder).toggle.setChecked(false);
            }
        }
    }

    public void addToList() {
        Gson gson = new Gson();
        String json = gson.toJson(brightnessManagerList);
        editor = shref.edit();
        editor.putString(key, json);
        editor.commit();
    }

    int AD_TYPE = 1, CONTENT_TYPE = 0;

    @Override
    public int getItemViewType(int position) {
        if (position > 0 && position % Utils.AdPos == 0 && Utils.idLoad) {
            return AD_TYPE;
        }
        return CONTENT_TYPE;
    }

    public int getItemCount() {
//        return this.appList.size();
        int adPos = Utils.AdPos - 1;
        if (appList.size() > 0 && Utils.AdPos > 0 && appList.size() > Utils.AdPos) {
            additionalContent = appList.size() / adPos;
        }
        return appList.size() + additionalContent;
    }
}
