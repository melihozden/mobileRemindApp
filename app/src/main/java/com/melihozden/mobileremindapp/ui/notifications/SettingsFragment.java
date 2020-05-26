package com.melihozden.mobileremindapp.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.melihozden.mobileremindapp.R;

public class SettingsFragment extends Fragment {
    private SharedPreferences sharedPreferences ;

    private Switch darkModeSwitch,vibrationSwitch ;
    private Spinner dateTypeSpinner ;
    private String spinnerDateTypeOptions[] ;

    private  Spinner ringToneSpinner;
    private String spinnerRingToneOptions[];

    final String appPref = "App Pref" ;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        sharedPreferences = this.getActivity().getSharedPreferences(appPref,Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        spinnerDateTypeOptions = new String[2] ;
        spinnerRingToneOptions = new String[3] ;

        // connections

        darkModeSwitch = root.findViewById(R.id.darkmodeswitch);
        vibrationSwitch = root.findViewById(R.id.vibrationswitch);

        ringToneSpinner = root.findViewById(R.id.ringToneSpinner);

        spinnerRingToneOptions[0] = "TYPE_NOTIFICATION";
        spinnerRingToneOptions[1] = "TYPE_ALARM";
        spinnerRingToneOptions[2] = "TYPE_RINGTONE";

        spinnerDateTypeOptions[0] = "DD/MM/YYYY";
        spinnerDateTypeOptions[1] = "MM/DD/YYYY";

        dateTypeSpinner = root.findViewById(R.id.datespinner);

        ArrayAdapter<String> ringtoneAdapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_spinner_item,spinnerRingToneOptions) ;
        ringToneSpinner.setAdapter(ringtoneAdapter);



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_spinner_item,spinnerDateTypeOptions) ;
        dateTypeSpinner.setAdapter(adapter);

        ringToneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(ringToneSpinner.getSelectedItemPosition() == 0){
                    editor.putString("ringType","TYPE_NOTIFICATION" );
                }
                else if(ringToneSpinner.getSelectedItemPosition() == 1){
                    editor.putString("ringType","TYPE_ALARM") ;
                }
                else if(ringToneSpinner.getSelectedItemPosition() == 2){
                    editor.putString("ringType","TYPE_RINGTONE") ;
                }
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dateTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(dateTypeSpinner.getSelectedItemPosition() == 0){
                    editor.putString("dateType","DD/MM/YYYY" );
                }
                else if(dateTypeSpinner.getSelectedItemPosition() == 1){
                    editor.putString("dateType","MM/DD/YYYY") ;
                }
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(darkModeSwitch.isChecked()){
                    editor.putString("darkMode","1");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else{
                    editor.putString("darkMode","0");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                editor.commit();
            }
        });

        vibrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(vibrationSwitch.isChecked()){
                    editor.putString("vibrationMode","1");
                }
                else{
                    editor.putString("vibrationMode","0");
                }
                editor.commit();
            }
        });


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        settingPrefRead();
    }

    public void settingPrefRead(){

        SharedPreferences sh = getActivity().getSharedPreferences(appPref,Context.MODE_PRIVATE);

        // darkMode
        if(sh.getString("darkMode","0").equals("0")){

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            darkModeSwitch.setChecked(false);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkModeSwitch.setChecked(true);
        }

        // vibrationMode
        if(sh.getString("vibrationMode","0").equals("0")){
            vibrationSwitch.setChecked(false);
        }
        else{
            vibrationSwitch.setChecked(true);
        }

        // dateType
        if(sh.getString("dateType","DD/MM/YYYY").equals("DD/MM/YYYY")){
            dateTypeSpinner.setSelection(0);
        }
        else{
            dateTypeSpinner.setSelection(1);
        }

        // ringType
        if(sh.getString("ringType","TYPE_NOTIFICATION").equals("TYPE_NOTIFICATION")){
            ringToneSpinner.setSelection(0);
        }
        else if(sh.getString("ringType","TYPE_ALARM").equals("TYPE_ALARM")){
            ringToneSpinner.setSelection(1);
        }
        else{
            ringToneSpinner.setSelection(2);
        }
    }




}