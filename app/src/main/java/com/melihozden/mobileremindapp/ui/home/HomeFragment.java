package com.melihozden.mobileremindapp.ui.home;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.melihozden.mobileremindapp.AlarmReceiver;
import com.melihozden.mobileremindapp.MainActivity;
import com.melihozden.mobileremindapp.NewReminderActivity;
import com.melihozden.mobileremindapp.R;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class HomeFragment extends Fragment implements RecyclerViewClickInterface {

    private FloatingActionButton floatingActionButton ;
    FirebaseFirestore firebaseFirestore ;

    ArrayList<Alert> alertList;
    AlertAdapter alertAdapter;

    SwipeRefreshLayout swipeRefreshLayout ;
    RecyclerView recyclerView ;

    TextView countTextview ;
    ImageView reminderLogo;

    Map<String,Object> data;
    ArrayList<String> alertIDs = new ArrayList<>() ;


    int mYear, mMonth, mDay, mHour, mMinute;
    final Calendar calendar = Calendar.getInstance();

    int countDone = 0 ;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        alertList = new ArrayList<Alert>();
        alertList.clear();

        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        alertAdapter = new AlertAdapter(getActivity(), alertList,this);

        //adapter.setClickListener(this);
        recyclerView.setAdapter(alertAdapter);

        floatingActionButton = root.findViewById(R.id.floatActionButton);

        // to count how many task is done
        countTextview = root.findViewById(R.id.countTextview);

        firebaseFirestore = FirebaseFirestore.getInstance();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewReminderActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout = root.findViewById(R.id.refreshLayout);
        reminderLogo = root.findViewById(R.id.reminderLogo);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                readData();
                alertAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return root;
    }

    String deletedAlertTitle = null ;
    int position = -1 ;

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {


        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            position = viewHolder.getAdapterPosition();

            switch (direction){
                case ItemTouchHelper.LEFT:

                    // Firebase delete codes will come here
                     deleteData(position);
                    deletedAlertTitle = alertList.get(position).getAlertTitle();
                    alertList.remove(position);
                    alertAdapter.notifyItemRemoved(position);
                    Toast.makeText(getActivity(), deletedAlertTitle + " has been removed", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(getActivity(), R.color.swipeDelete))
                    .addActionIcon(R.drawable.ic_clear_black_24dp)
                    .create()
                    .decorate();
        }

    };

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("App Pref", Context.MODE_PRIVATE);
        String isDarkModeOn = sharedPreferences.getString("darkMode","0");
        if(isDarkModeOn.equals("0")){
            reminderLogo.setImageResource(R.drawable.blacktrans2x);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else{
            reminderLogo.setImageResource(R.drawable.whitetrans2x);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        readData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void readData(){

        CollectionReference collectionReference = firebaseFirestore.collection("newAlert");

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                alertList.clear();
                if(alertAdapter.getItemCount() == 0){
                    countTextview.setText("No Reminder Yet");
                }
                if (queryDocumentSnapshots!=null){
                    countDone = 0;
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){

                        data = snapshot.getData();
                        alertIDs.add(snapshot.getId());
                        String reminderTitle = (String) data.get("alertTitle");
                        String reminderDescription = (String) data.get("alertDescription");
                        String reminderDate =  (String) data.get("alertDate");
                        String reminderColor = (String) data.get("alertColor");
                        String reminderDateString = (String) data.get("alertDateString");
                        String reminderTimeString = (String) data.get("alertTimeString");
                        String reminderRepeat = (String) data.get("alertRepeat");
                        String isPriority = (String) data.get("isPriority");
                        String isActive = (String) data.get("isActive");

                        alertList.add(new Alert(reminderTitle,reminderDescription,reminderDate,
                                reminderDateString,reminderTimeString,reminderColor,
                                reminderRepeat,isPriority,isActive));
                        alertAdapter.notifyDataSetChanged();

                        if(isActive.equals("false")){
                            countDone++ ;
                        }

                        /*
                        System.out.println("ALERT ID : " + alertID);
                        System.out.println(reminderTitle);
                        System.out.println(reminderDescription);
                        System.out.println(reminderDate);
                        System.out.println(reminderColor);
                        System.out.println(reminderRepeat);
                        System.out.println(isPriority);
                        System.out.println(isActive);
                        System.out.println("ITEM COUNT : "+alertAdapter.getItemCount());
                        System.out.println("-----------");
*/
                        countTextview.setText(countDone+"/"+alertAdapter.getItemCount()+" Completed");
                    }
                }
            }
        });
    }

    public void deleteData(int position){

        if(position != -1){
            firebaseFirestore.collection("newAlert").document(alertIDs.get(position)).delete();
            //alarmManager.cancel(pendingIntent);
            alertIDs.remove(position);
            alertAdapter.notifyItemRemoved(position);
            //readData();
        }
}


    String repeatArray[];
    Spinner spinner ;

    EditText dialogReminderTitle,dialogReminderDescription;
    Button dialogDateButton, dialogTimeButton;

    Switch dialogReminderSwitch ;

    RadioGroup dialogRadioGrup;

    Button dialogEditButton,dialogDeleteButton,dialogShareButton ;

    // When item pressed to edit reminder
    @Override
    public void onItemClick(final int position) {



        //dialogReminderTitle.setText(String.valueOf(alertList.get(position).getAlertTitle()));
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_edit_reminder_dialog);

        // ----EDIT REMINDER_DATE----

        dialogDateButton = dialog.findViewById(R.id.dialogDateButton);

        dialogDateButton.setText(alertList.get(position).getAlertDateString());
        dialogDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH);
                mDay = calendar.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {


                                dialogDateButton.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                                calendar.set(Calendar.MONTH,monthOfYear);
                                calendar.set(Calendar.YEAR,year);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });


        // ----END EDIT REMINDER_DATE----

        // ----EDIT REMINDER_TIME----
        dialogTimeButton = dialog.findViewById(R.id.dialogTimeButton);

        dialogTimeButton.setText(alertList.get(position).getAlertTimeString());
        dialogTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                dialogTimeButton.setText(hourOfDay + ":" + minute);
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
        // ----END REMINDER_TIME----

        // ----------EDIT SPINNER------------
        repeatArray = new String[4];
        repeatArray[0] = "No Repeat";
        repeatArray[1] = "Every Day";
        repeatArray[2] = "Every 2 Days";
        repeatArray[3] = "Every Week";

        spinner = dialog.findViewById(R.id.dialogRepeatSpinner);
        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,repeatArray);
        spinner.setAdapter(arrayAdapter);
        if(alertList.get(position).getAlertRepeat().equals("No Repeat")){
            spinner.setSelection(0);
        }
        else if(alertList.get(position).getAlertRepeat().equals("Every Day")){
            spinner.setSelection(1);
        }
        else if(alertList.get(position).getAlertRepeat().equals("Every 2 Days")){
            spinner.setSelection(2);
        }
        else if(alertList.get(position).getAlertRepeat().equals("Every Week")){
            spinner.setSelection(3);
        }
        // END--------EDIT SPINNER------------

        // ----EDIT REMINDER_TITLE----
        dialogReminderTitle = (EditText) dialog.findViewById(R.id.dialogReminderTitle);
        dialogReminderTitle.setText(alertList.get(position).getAlertTitle());
        // ----END REMINDER_TITLE-----

        // ----EDIT REMINDER_DESCRIPTION----
        dialogReminderDescription = dialog.findViewById(R.id.dialogDescription);
        dialogReminderDescription.setText(alertList.get(position).getAlertDescription());

        // ----END REMINDER_DESCRIPTION----

        // ----EDIT REMINDER_PRIORITY-----
        dialogReminderSwitch = dialog.findViewById(R.id.dialogPrioritySwitch);
        if(alertList.get(position).getIsPriority().equals("true")){
            dialogReminderSwitch.setChecked(true);
        }
        else{
            dialogReminderSwitch.setChecked(false);
        }

        // ----END REMINDER_PRIORITY-----

        // ----EDIT REMINDER_COLOR_RADIO-----

        dialogRadioGrup = dialog.findViewById(R.id.dialogRadioGrup);
                if(alertList.get(position).getAlertColor().equals("#E54B4B")){
                    ((RadioButton)dialogRadioGrup.getChildAt(0)).setChecked(true);
                }
                else if(alertList.get(position).getAlertColor().equals("#4591D4")){
                    ((RadioButton)dialogRadioGrup.getChildAt(1)).setChecked(true);
                }
                else if(alertList.get(position).getAlertColor().equals("#FFBB00")){
                    ((RadioButton)dialogRadioGrup.getChildAt(2)).setChecked(true);
                }
                else if(alertList.get(position).getAlertColor().equals("#02D81F")){
                    ((RadioButton)dialogRadioGrup.getChildAt(3)).setChecked(true);
                }
                else if(alertList.get(position).getAlertColor().equals("#DB00EB")){
                    ((RadioButton)dialogRadioGrup.getChildAt(4)).setChecked(true);
                }
                else if(alertList.get(position).getAlertColor().equals("#6E6E6E")){
                    ((RadioButton)dialogRadioGrup.getChildAt(5)).setChecked(true);
                }

        // ----END REMINDER_COLOR_RADIO-----


        // ----EDIT BUTTON------
        dialogEditButton = dialog.findViewById(R.id.dialogEditButton);
        dialogEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //editData
                editData(position);
                //alarmManager
                dialog.dismiss();

            }
        });

        dialog.show();


        // ----END BUTTON------

        // ----EDIT DELETE BUTTON-----
        dialogDeleteButton = dialog.findViewById(R.id.dialogDeleteButton);
        dialogDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData(position);
                Toast.makeText(getActivity(), "Reminder Deleted..", Toast.LENGTH_SHORT).show();
                alertList.remove(position);
                dialog.dismiss();
            }
        });

        // ----EDIT SHARE BUTTON---
        dialogShareButton = dialog.findViewById(R.id.dialogShareButton);
        dialogShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });



        // ----END SHARE BUTTON----


        // ----END DELETE BUTTON-----
    }

    public boolean alarmControl(String title, String date, String time){

        if(title.equals("") || date.equals("Set Date") || time.equals("Set Tıme")){
            return false ;
        }
        return true ;
    }

    public void sendMail(){

        if(alarmControl(dialogReminderTitle.getText().toString(),dialogDateButton.getText().toString(),dialogDateButton.getText().toString())){

        try {
            String stringEmailTopic = dialogReminderTitle.getText().toString();
            String stringEmailDesc = dialogReminderDescription.getText().toString() + "Date : "+dialogDateButton.getText().toString()+"Time : "+dialogTimeButton.getText().toString();

            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, stringEmailTopic);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, stringEmailDesc);
            this.startActivity(Intent.createChooser(emailIntent, "Araciligiyla gonder..."));

        } catch (Throwable t) {
            Toast.makeText(getActivity(), "Share Failed.."+ t.toString(), Toast.LENGTH_LONG).show();
        }
        }
        else{
            Toast.makeText(getActivity(),"Please Fill Correctly Before Share",Toast.LENGTH_LONG).show();
        }

    }

    public void editData(int position){

        int selectedId = dialogRadioGrup.getCheckedRadioButtonId();

        String title = dialogReminderTitle.getText().toString();
        String description = dialogReminderDescription.getText().toString();
        String date = dialogDateButton.getText().toString();
        String time = dialogTimeButton.getText().toString();
        String spinnerValue = spinner.getSelectedItem().toString();
        String isActive = alertList.get(position).getIsActive();
        String isPriority = "" ;
        if(dialogReminderSwitch.isChecked()){
            isPriority = "true";
        }
        else{
            isPriority = "false";
        }
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

            HashMap<String,Object> editAlertMap = new HashMap<>();
            editAlertMap.put("alertTitle",title);
            editAlertMap.put("alertDescription",description);
            editAlertMap.put("alertRepeat",spinnerValue);
            editAlertMap.put("alertDate",String.valueOf(calendar.getTimeInMillis()));
            editAlertMap.put("alertDateString",date);
            editAlertMap.put("alertTimeString",time);
            editAlertMap.put("alertColor",color);
            editAlertMap.put("isPriority",isPriority);
            editAlertMap.put("isActive",isActive);

            firebaseFirestore.collection("newAlert").document(alertIDs.get(position)).update(editAlertMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(),"Edit Success",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }

        else{
            Toast.makeText(getActivity(),"Please Fill Correctly",Toast.LENGTH_LONG).show();
        }

    }



    @Override
    public void onLongItemClick(int position) {

        if(alertList.get(position).getIsActive().equals("true")){
            firebaseFirestore.collection("newAlert").document(alertIDs.get(position)).update("isActive","false");
            Toast.makeText(getActivity(), alertList.get(position).getAlertTitle()+" is done now", Toast.LENGTH_SHORT).show();
        }
        else{
            firebaseFirestore.collection("newAlert").document(alertIDs.get(position)).update("isActive","true");
            Toast.makeText(getActivity(), alertList.get(position).getAlertTitle()+" is not done now", Toast.LENGTH_SHORT).show();
        }
    }
}