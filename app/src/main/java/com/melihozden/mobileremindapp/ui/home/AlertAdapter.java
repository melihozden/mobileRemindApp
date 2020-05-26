package com.melihozden.mobileremindapp.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.melihozden.mobileremindapp.R;

import java.util.ArrayList;


public class AlertAdapter  extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    SharedPreferences sharedPreferences ;

    private ArrayList<Alert> mAlertList;
    private LayoutInflater mInflater;
    private RecyclerViewClickInterface recyclerViewClickInterface;

    String transformDate[];

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alert alert = mAlertList.get(position);
        holder.setData(alert,position);
    }

    // data is passed into the constructor
    AlertAdapter(Context context, ArrayList<Alert> alerts, RecyclerViewClickInterface recyclerViewClickInterface) {
        this.mInflater = LayoutInflater.from(context);
        this.mAlertList = alerts;
        this.recyclerViewClickInterface = recyclerViewClickInterface;
        sharedPreferences = context.getSharedPreferences("App Pref",Context.MODE_PRIVATE);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.alerts_card, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    // total rows
    @Override
    public int getItemCount() {
        return mAlertList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView alertTitle,alertDate,alertTime ;
        LinearLayout alertLayout;
        ImageView checkSign,flagSign ;

        ViewHolder(View itemView) {
            super(itemView);

            alertTitle = itemView.findViewById(R.id.alertTitle);
            alertDate = itemView.findViewById(R.id.alertDate);
            alertTime = itemView.findViewById(R.id.alertTime) ;
            alertLayout = itemView.findViewById(R.id.alertLayout);
            checkSign = itemView.findViewById(R.id.checkSign);
            flagSign = itemView.findViewById(R.id.flagSign);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewClickInterface.onItemClick(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    recyclerViewClickInterface.onLongItemClick(getAdapterPosition());
                    return true;
                }
            });
        }


        public void setData(Alert selectedAlert, int position){

            this.alertTitle.setText(selectedAlert.getAlertTitle());
            if(sharedPreferences.getString("dateType","DD/MM/YYYY").equals("MM/DD/YYYY")){
                String transformedDate = dateTransfrom(selectedAlert.getAlertDateString());
                this.alertDate.setText(transformedDate);
            }
            else{
                this.alertDate.setText(selectedAlert.getAlertDateString());
            }
            this.alertTime.setText(selectedAlert.getAlertTimeString());
            this.alertLayout.setBackgroundColor(Color.parseColor(selectedAlert.getAlertColor()));
            if(selectedAlert.getIsActive().equals("false")){
                this.checkSign.setImageResource(R.drawable.ic_check_black_24dp);
            }
            else{
                this.checkSign.setImageResource(0);
            }
            if(selectedAlert.getIsPriority().equals("true")){
                this.flagSign.setImageResource(R.drawable.ic_flag_black_24dp);
            }
            else{
                this.flagSign.setImageResource(0);
            }
        }
    }

    public String dateTransfrom(String date){

        transformDate = date.split("/");

        return transformDate[1]+"/"+transformDate[0]+"/"+transformDate[2];
    }

}
