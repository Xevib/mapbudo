package com.osm.mapbudo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.graphics.drawable.Drawable;
import android.util.Pair;

public class POIType {
	HashMap<String,String> heredated;
	Drawable icon;
	String name;
    List<Field> fields;
	public POIType(String name,HashMap<String,String> heredated )
	{
		this.name = name;
		this.heredated = heredated;
		this.icon = null;
        this.fields = new ArrayList<Field>();
	}
	
	
	public POIType(String name,HashMap<String, String> heredated, Drawable icon) {
		this.name=name;
		this.heredated=heredated;
		this.icon=icon;
	}


	public String getName()
	{
		return this.name;
	}
	public HashMap<String,String> getHeredated()
	{
		return this.heredated;
	}
	public Boolean match(HashMap<String,String> tags)
	{
		Iterator<Entry<String, String>> it=this.heredated.entrySet().iterator();
		Entry<String,String> element;
		while (it.hasNext())
		{
			element=it.next();
			if (tags.containsKey(element.getKey())) {
				if( !(tags.get(element.getKey()).equals( this.heredated.get(element.getKey())))){
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	public void setIcon(Drawable icon)
	{
		this.icon=icon;
	}
	public Drawable getIcon()
	{
		return this.icon;
	}
	public List<Pair<String,String>> getListValues()
	{
		List<Pair<String,String>> ret=new ArrayList<Pair<String,String>>();
		Iterator<Entry<String,String>> it=this.heredated.entrySet().iterator();
		Entry<String,String>element;
		while (it.hasNext()) {
			element=it.next();
			ret.add(new Pair<String,String>(element.getKey(),element.getValue()));
		}
		return ret;
	}
    public void addField(Field f)
    {
        this.fields.add(f);
    }
}
