package com.melihozden.mobileremindapp.ui.home;

import android.content.Context;
import android.content.SharedPreferences;

public class Alert {

    private String alertTitle ;
    private String alertDescription ;
    private String alertDate;
    private String alertDateString;
    private String alertTimeString;
    private String alertColor ;
    private String alertRepeat ;
    private String isPriority ;
    private String isActive ;

    public Alert(String alertTitle,String alertDescription,
                 String alertDate, String alertDateString,String alertTimeString, String alertColor, String alertRepeat,
                 String isPriority, String isActive ) {
        this.alertTitle = alertTitle;
        this.alertDescription = alertDescription;
        this.alertDate = alertDate;
        this.alertDateString = alertDateString;
        this.alertTimeString = alertTimeString;
        this.alertColor = alertColor;
        this.alertRepeat = alertRepeat;
        this.isPriority = isPriority;
        this.isActive = isActive;
    }

    public String getAlertTitle() {
        return alertTitle;
    }

    public String getAlertDate() {
        return alertDate;
    }

    public String getAlertColor() {
        return alertColor;
    }

    public String getAlertDateString() {

        return alertDateString;
    }

    public String getAlertTimeString() {
        return alertTimeString;
    }

    public String getAlertDescription() {
        return alertDescription;
    }

    public String getAlertRepeat() {
        return alertRepeat;
    }

    public String getIsPriority() {
        return isPriority;
    }

    public String getIsActive() {
        return isActive;
    }

}
