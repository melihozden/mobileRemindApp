package com.melihozden.mobileremindapp.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.melihozden.mobileremindapp.R;
import com.melihozden.mobileremindapp.ui.home.Alert;
import com.melihozden.mobileremindapp.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.Map;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.vo.DateData;
import sun.bob.mcalendarview.vo.MarkedDates;

public class CalendarFragment extends Fragment {


    FirebaseFirestore firebaseFirestore ;
    MCalendarView mCalendarView ;
    Map<String,Object> data ;
    ArrayList<DateData> dateData ;
    String date[] ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard,container,false);

        mCalendarView =  root.findViewById(R.id.calendar);

        firebaseFirestore = FirebaseFirestore.getInstance();

        calendarAction();

        MarkedDates markedDates = mCalendarView.getMarkedDates();
        dateData = markedDates.getAll();

        for(int i = 0 ; i<dateData.size();i++){
            System.out.println(dateData.get(i));
        }

        return root ;



    }

    @Override
    public void onResume() {
        super.onResume();
       // readDataAndWriteCalendar();
    }

    @Override
    public void onStart() {
        super.onStart();
        readDataAndWriteCalendar();
    }

    public void calendarAction(){

        mCalendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {


                Toast.makeText(getActivity(),"Tarih Bastın...",Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void readDataAndWriteCalendar(){

        CollectionReference collectionReference = firebaseFirestore.collection("newAlert");

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (queryDocumentSnapshots!=null){
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                        data = snapshot.getData();

                        String reminderDateString = (String) data.get("alertDateString");
                        String reminderColor = (String) data.get("alertColor");
                        date = reminderDateString.split("/");
                        mCalendarView.markDate(new DateData(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0])).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.parseColor(reminderColor))));
                    }
                }
            }
        });
    }


}