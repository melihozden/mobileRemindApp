package com.melihozden.mobileremindapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class NewReminderActivity extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore ;

    // AlarmManager
    AlarmManager alarmManager ;
    // PendingIntent
    PendingIntent pendingIntent ;

    // to set date and time
    Button dateButton,timeButton;

    // to save firebase
    Button saveButton;

    // title & description
    EditText reminderTitle, reminderDescription ;

    // to fill the repeater spinner
    Spinner spinner ;

    // radiobuttongroup
    RadioGroup radioGroup ;

    // switch to set priority
    Switch prioritySwitch;

    // toast Message
    TextView toastTextview ;

    LayoutInflater layoutInflater;
    View layout ;

    // to Set Multi Alarm
    ArrayList<AlarmManager> alarmManagers ;


    String repeatArray[];

    int mYear, mMonth, mDay, mHour, mMinute;
    int selectedId ;

    final Context context = this ;
    final Calendar calendar = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reminder);

        firebaseFirestore = FirebaseFirestore.getInstance();

        layoutInflater = getLayoutInflater() ;
        layout = layoutInflater.inflate(R.layout.toast_message, (ViewGroup) findViewById(R.id.toastLayout));

        // title and description textbox connection
        reminderTitle = findViewById(R.id.newTitle);
        reminderDescription = findViewById(R.id.newDescription);

        // date and time button connection for showing dialog box
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);

        //radiogroup
        radioGroup = findViewById(R.id.radioGroup);

        toastTextview = findViewById(R.id.toastMessage);

        prioritySwitch = findViewById(R.id.prioritySwitch);

        // fill the spinner content
        repeatArray = new String[4];

        repeatArray[0] = "No Repeat";
        repeatArray[1] = "Every Day";
        repeatArray[2] = "Every 2 Days";
        repeatArray[3] = "Every Week";

        spinner = findViewById(R.id.repeatSpinner);
        ArrayAdapter arrayAdapter = new ArrayAdapter(NewReminderActivity.this,android.R.layout.simple_spinner_item,repeatArray);
        spinner.setAdapter(arrayAdapter);

        // firebase save button connection
        saveButton = findViewById(R.id.savealertbutton) ;

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH);
                mDay = calendar.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(NewReminderActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {


                                dateButton.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                                calendar.set(Calendar.MONTH,monthOfYear);
                                calendar.set(Calendar.YEAR,year);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(NewReminderActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                timeButton.setText(hourOfDay + ":" + minute);
                                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                calendar.set(Calendar.MINUTE,minute);
                                calendar.set(Calendar.SECOND,0);
                            }
                        },mHour,mMinute,true
                );
                // Time selection.. control

                timePickerDialog.show();
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addData();
            }
        });

    }

    public void addData(){

        selectedId = radioGroup.getCheckedRadioButtonId();

        String title = reminderTitle.getText().toString();
        String description = reminderDescription.getText().toString();
        String date = dateButton.getText().toString();
        String time = timeButton.getText().toString();
        String spinnerValue = spinner.getSelectedItem().toString();
        String isPriority = "" ;
        if(prioritySwitch.isChecked()){
            isPriority = "true";
        }
        else{
            isPriority = "false";
        }
        String isActive = "true";
        String color = "" ;

        if(selectedId % 6 == 1){
            color = "#E54B4B" ;
        }
        else if(selectedId % 6 == 2){
            color = "#4591D4" ;

        }
        else if(selectedId % 6 == 3){
            color = "#FFBB00" ;

        }
        else if(selectedId % 6 == 4){
            color = "#02D81F" ;

        }
        else if(selectedId % 6 == 5){
            color = "#DB00EB" ;

        }
        else if(selectedId % 6 == 0 || selectedId == 6){
            color = "#6E6E6E" ;

        }
        else if(selectedId == -1){
            color = "#FFFFFF";
        }

        if(alarmControl(title,date,time)){


        HashMap<String,Object> alertMap = new HashMap<>();


        alertMap.put("alertTitle",title);
        alertMap.put("alertDescription",description);
        alertMap.put("alertRepeat",spinnerValue);
        alertMap.put("alertDate",String.valueOf(calendar.getTimeInMillis()));
        alertMap.put("alertDateString",date);
        alertMap.put("alertTimeString",time);
        alertMap.put("alertColor",color);
        alertMap.put("isPriority",isPriority);
        alertMap.put("isActive",isActive);


            firebaseFirestore.collection("newAlert").add(alertMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                setAlarm();
                //alarmInfo();
                //alarmCancel();
                Intent intent = new Intent(NewReminderActivity.this,MainActivity.class);
                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewReminderActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });
        }

        else{
            Toast.makeText(NewReminderActivity.this,"Please Fill Correctly",Toast.LENGTH_LONG).show();
        }
    }

    public boolean alarmControl(String title, String date, String time){

        if(title.equals("") || date.equals("Set Date") || time.equals("Set Tıme")){
            return false ;
        }

        return true ;
    }


    public void setAlarm(){

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 1, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        // Burada tekrarlanma durumlarına göre methodlar değişecek.

        if(spinner.getSelectedItem().toString().equals("No Repeat")){
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        // Every Day set alarm
        else if (spinner.getSelectedItem().toString().equals("Every Day")){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),24*60*60*1000,pendingIntent);
        }
        // Every 2 days set alarm
        else if(spinner.getSelectedItem().toString().equals("Every 2 Days")){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),2*24*60*60*1000,pendingIntent);
        }
        // Weekly Set Alarm..
        else if(spinner.getSelectedItem().toString().equals("Every Week")){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY*7,pendingIntent);
        }
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM,0,40);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();


        // To set Alarm here with AlarmManager

    }

    public void alarmCancel(){
        alarmManager.cancel(pendingIntent);
    }


    public void alarmInfo(){
        System.out.println("Alarm INFO : " + alarmManager.getNextAlarmClock());
    }

}
