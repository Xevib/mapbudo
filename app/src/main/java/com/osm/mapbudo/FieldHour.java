package com.osm.mapbudo;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.edmodo.rangebar.RangeBar;


public class FieldHour {
    Dialog dialog;
    Button addOpen;
    Button dialogButtonOK;
    TextView txtHours;
    ToggleButton btMonday;
    ToggleButton btTuesday;
    ToggleButton btWednesday;
    ToggleButton btThursday;
    ToggleButton btFriday;
    ToggleButton btSaturday;
    ToggleButton btSunday;
    RangeBar slRange;
    String value;


    public FieldHour(Context c) {
        dialog = new Dialog(c);
        dialog.setContentView(R.layout.dialog_hours);
        dialog.setTitle("Opening hours");
        addOpen = (Button) dialog.findViewById(R.id.addOpen);
        txtHours = (TextView) dialog.findViewById(R.id.txtHours);

        btMonday = (ToggleButton) dialog.findViewById(R.id.btMonday);
        btTuesday = (ToggleButton) dialog.findViewById(R.id.btTuesday);
        btWednesday = (ToggleButton) dialog.findViewById(R.id.btWednesday);
        btThursday = (ToggleButton) dialog.findViewById(R.id.btThursday);
        btFriday = (ToggleButton) dialog.findViewById(R.id.btFriday);
        btSaturday = (ToggleButton) dialog.findViewById(R.id.btSaturday);
        btSunday = (ToggleButton) dialog.findViewById(R.id.btSunday);
        slRange = (RangeBar) dialog.findViewById(R.id.slRange);

        dialogButtonOK = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ;
            }
        });
        dialogButtonOK.setTag("");
        addOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String days = "";
                if (btMonday.isChecked()) {
                    days = days + "Mo";
                    if (days.length() != 0) {
                        days = days + "|";
                    }
                }
                if (btTuesday.isChecked()) {
                    days = days + "Tu";
                    if (days.length() != 0) {
                        days = days + "|";
                    }
                }
                if (btWednesday.isChecked()) {
                    days = days + "We";
                    if (days.length() != 0) {
                        days = days + "|";
                    }
                }
                if (btThursday.isChecked()) {
                    days = days + "Th";
                    if (days.length() != 0) {
                        days = days + "|";
                    }
                }
                if (btFriday.isChecked()) {
                    days = days + "Fr";
                    if (days.length() != 0) {
                        days = days + "|";
                    }
                }
                if (btSaturday.isChecked()) {
                    days = days + "Sa";
                    if (days.length() != 0) {
                        days = days + "|";
                    }
                }
                if (btSunday.isChecked()) {
                    days = days + "Su";
                    if (days.length() != 0) {
                        days = days + "|";
                    }
                }
                int hr_start = (slRange.getLeftIndex() / 30);
                int min_start = (slRange.getLeftIndex() % 30);

                int hr_close = (slRange.getRightIndex() / 30);
                int min_close = (slRange.getRightIndex() % 30);

                txtHours.setText("[" + days.substring(0, days.length() - 1) + "] " + String.valueOf(hr_start) + ":" + String.valueOf(min_start) + "-" + String.valueOf(hr_close) + ":" + String.valueOf(min_close));
                value = "[" + days.substring(0, days.length() - 1) + "] " + String.valueOf(hr_start) + ":" + String.valueOf(min_start) + "-" + String.valueOf(hr_close) + ":" + String.valueOf(min_close);
                dialogButtonOK.setTag(value);

            }

        });
    }
    public FieldHour(Context c, String init_value)
    {
        this.value=init_value;
        dialog = new Dialog(c);
        dialog.setContentView(R.layout.dialog_hours);
        dialog.setTitle("Opening hours");
        addOpen=(Button) dialog.findViewById(R.id.addOpen);
        txtHours=(TextView) dialog.findViewById(R.id.txtHours);
        txtHours.setText(this.value);

        btMonday=(ToggleButton)dialog.findViewById(R.id.btMonday);
        btTuesday=(ToggleButton)dialog.findViewById(R.id.btTuesday);
        btWednesday=(ToggleButton)dialog.findViewById(R.id.btWednesday);
        btThursday=(ToggleButton)dialog.findViewById(R.id.btThursday);
        btFriday=(ToggleButton)dialog.findViewById(R.id.btFriday);
        btSaturday=(ToggleButton)dialog.findViewById(R.id.btSaturday);
        btSunday=(ToggleButton)dialog.findViewById(R.id.btSunday);
        slRange=(RangeBar)dialog.findViewById(R.id.slRange);

        dialogButtonOK=(Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();;
            }
        });
        dialogButtonOK.setTag("");
        addOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String days="";
                if( btMonday.isChecked()){
                    days=days+"Mo";
                    if( days.length()!=0) {
                        days=days+"|";
                    }
                }
                if (btTuesday.isChecked()){
                    days=days+"Tu";
                    if( days.length()!=0) {
                        days=days+"|";
                    }
                }
                if(btWednesday.isChecked()) {
                    days=days+"We";
                    if( days.length()!=0) {
                        days=days+"|";
                    }
                }
                if(btThursday.isChecked()) {
                    days=days+"Th";
                    if( days.length()!=0) {
                        days=days+"|";
                    }
                }
                if(btFriday.isChecked()) {
                    days=days+"Fr";
                    if( days.length()!=0) {
                        days=days+"|";
                    }
                }
                if(btSaturday.isChecked()) {
                    days=days+"Sa";
                    if( days.length()!=0) {
                        days=days+"|";
                    }
                }
                if(btSunday.isChecked()) {
                    days=days+"Su";
                    if( days.length()!=0) {
                        days=days+"|";
                    }
                }
                int hr_start=(slRange.getLeftIndex()/30);
                int min_start=(slRange.getLeftIndex()%30);
                int hr_close=(slRange.getRightIndex()/30);
                int min_close=(slRange.getRightIndex()%30);

                txtHours.setText("["+days.substring(0,days.length()-1)+"] "+String.valueOf(hr_start)+":"+String.valueOf(min_start)+"-"+String.valueOf(hr_close)+":"+String.valueOf(min_close));
                value="["+days.substring(0,days.length()-1)+"] "+String.valueOf(hr_start)+":"+String.valueOf(min_start)+"-"+String.valueOf(hr_close)+":"+String.valueOf(min_close);
                dialogButtonOK.setTag(value);
            }

        });

    }
    public void onOK(View.OnClickListener listener)
    {
        Button dialogButtonOK=(Button)dialog.findViewById(R.id.dialogButtonOK);
        dialogButtonOK.setOnClickListener(listener);
    }
    public void show()
    {
        this.dialog.show();
    }
}
