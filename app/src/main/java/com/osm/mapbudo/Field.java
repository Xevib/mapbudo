package com.osm.mapbudo;

import android.app.Activity;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;





public class Field {
    String title;
    String xml_view;
    public Field(String title,String xml) {
        this.title = title;
        this.xml_view = xml;
        this.xml_view="<TextView android:id=\"@+id/textView1\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" app:layout_gravity=\"left\" android:text=\"@string/name\" /><EditText android:id=\"@+id/etName\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:layout_weight=\"1\" app:layout_gravity=\"left|top\" android:ems=\"10\" ></EditText>";
    }
    public ViewGroup addView(Activity activity,ViewGroup root) {
        LayoutInflater inflater = activity.getLayoutInflater();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            InputStream is = new ByteArrayInputStream(this.xml_view.getBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, "UTF-8");
            inflater.inflate(parser, root, true);
            return root;
        }
        catch (XmlPullParserException e)
        {
            return null;
        }

    }
}




