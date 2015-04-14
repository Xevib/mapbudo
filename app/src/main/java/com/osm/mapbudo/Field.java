package com.osm.mapbudo;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.util.HashMap;


public class Field {
    TextView title;
    Integer type;
    EditText et;
    String key;

    public Field(Activity activity,String t,String key) {
        this.title = new TextView(activity);
        this.title.setText(t);
        this.type = null;
        this.et = null;
        this.key = key;
    }
    public void addView(Activity activity,ViewGroup root) {
        ViewGroup grid= (ViewGroup)root.findViewById(R.id.grid);
        LayoutInflater inflater = activity.getLayoutInflater();
        this.et = new EditText(activity);
        this.et.setEms(10);
        if (this.type!=null) {
            et.setInputType(this.type);
        }
        grid.addView(et, 0);
        grid.addView(this.title, 0);
    }
    public void setType(int type){
        this.type = type;
    }
    public void setKey(String key) {
        this.key=key;
    }
    public HashMap<String,String> getData() {
        HashMap<String,String> ret= new HashMap<String,String>();
        ret.put(this.key,this.et.getText().toString());
        return ret;
    }
    public String getValue() {
        String r=this.et.getText().toString();
        if (r.length()==0) {
            return "";
        }
        if ( r.charAt(r.length()-1)==32) {
            return r.substring(0, r.length()-1);
        } else {
            return r;
        }
    }
    public String getXML() {
        if (!this.getValue().equalsIgnoreCase("")) {

            return "<tag k='" + this.key + "' v='" + this.getValue() + "'/>";
        } else {
            return "";
        }
    }
}




