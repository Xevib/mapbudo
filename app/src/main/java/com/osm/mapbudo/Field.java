package com.osm.mapbudo;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public List<View> getView(Activity activity) {
        List<View> l= new ArrayList<View>();
        this.et = new EditText(activity);
        this.et.setEms(10);
        if (this.type!=null) {
            et.setInputType(this.type);
        }
        l.add(this.et);
        l.add(this.title);
        return l;
    }
    public void addView(Activity activity,ViewGroup root) {
        ViewGroup grid= (ViewGroup)root.findViewById(R.id.grid);
        for(View v:this.getView(activity)) {
            grid.addView(v,0);
        }
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
