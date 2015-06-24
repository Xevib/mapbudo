package com.osm.mapbudo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;


public class Filter {
	HashMap <POIType,Boolean> filter;
	HashMap<String,List<POIType>> groups;
	List<String> group_names;
	List<POIType> avaible_types;
	HashMap<POIType, Drawable> icons;
	public Filter()
	{
		this.filter=new HashMap<POIType,Boolean>();
		this.groups=new HashMap<String,List<POIType>>();
		this.icons=new HashMap<POIType,Drawable>();
		this.group_names=new ArrayList<String>();
		this.avaible_types=new ArrayList<POIType>();
		
	}
	public void enableType(POIType type)
	{
		Log.v("filter", type.getName()+" enabled");
		this.filter.put(type,true);
	}
	public void disableType(POIType type)
	{
		Log.v("filter", type.getName()+" disabled");
		this.filter.put(type,false);
	}
	public List <Pair<String,String>>  getActiveTypes()
	{
		List<Pair<String,String>> ret=new ArrayList<Pair<String,String>>();
		Iterator<Entry<POIType, Boolean>> it=filter.entrySet().iterator();
		Entry<POIType,Boolean> element;
		while (it.hasNext())
		{
			element=it.next();
			if (element.getValue())
			{
				ret.addAll(element.getKey().getListValues());
			}
		}
		return ret;
	}
	public List<POIType> getTypes()
	{
		return avaible_types;
	}
	public void disableAll()
	{
		Iterator<Entry<POIType, Boolean>> it=this.filter.entrySet().iterator();
		Entry<POIType,Boolean> element;
		while(it.hasNext())
		{
			element=it.next();
			this.filter.put(element.getKey(), false);
		}
	}
	public void enableAll()
	{
		Iterator<Entry<POIType, Boolean>> it=this.filter.entrySet().iterator();
		Entry<POIType,Boolean> element;
		while(it.hasNext())
		{
			element=it.next();
			this.filter.put(element.getKey(), true);
		}
	}
	
	public List<String> getGroups()
	{
		return this.group_names;
	}
	public void clearGroups()
	{
		groups.clear();
	}
	
	public void addGroup(String name,List<POIType> list)
	{
		this.group_names.add(name);
		this.groups.put(name, list);
		this.avaible_types.addAll(list);
		for (POIType t:list)
		{
			this.filter.put(t, false);
		}
		for (POIType t:list)
		{
			this.icons.put(t,t.getIcon());
		}
	}
	public POIType getType(String groupname,int elementindex)
	{
		return this.groups.get(groupname).get(elementindex);
	}
	public POIType getType(int groupIndex,int elementIndex)
	{
		return this.groups.get( this.group_names.get(groupIndex)).get(elementIndex);
	}

	public int getNumGroups() {
		return this.groups.size();
	}
	public int getNumElements(int groupPosition) {
		return this.groups.get( this.group_names.get(groupPosition)).size();
	}
	public String getGroup(int groupPosition) {
		
		return this.group_names.get(groupPosition);
	}
	public void save(SharedPreferences preferences) {
		Iterator<Entry<POIType, Boolean>> it=this.filter.entrySet().iterator();
		Entry <POIType,Boolean>element;
		while (it.hasNext())
		{
			
			element=it.next();
			preferences.edit().putBoolean("f".concat(element.getKey().getName()), element.getValue()).commit();
			
		}
	}
	public void load(SharedPreferences preferences)
	{
		Iterator<Entry<POIType, Boolean>> it = this.filter.entrySet().iterator();
		Entry<POIType,Boolean> element;
		while (it.hasNext())
		{
			element=it.next();
			this.filter.put(element.getKey(), preferences.getBoolean("f".concat(element.getKey().getName()),false));
		}
	}
	public Boolean getStatus(String typename)
	{
		Iterator<Entry<POIType, Boolean>> it=this.filter.entrySet().iterator();
		Entry<POIType, Boolean> element = null;
		Boolean stop=false;
		while((it.hasNext())&&(!stop)) {
			element=it.next();
			if (element.getKey().getName().equalsIgnoreCase(typename)) {
				stop=true;
			}
		}
		if (stop) {
			return element.getValue();
		} else {
			return null;
		}
	}
	
	
	public Drawable getIcon(String typename) {
		Iterator<Entry<POIType, Drawable>> it=this.icons.entrySet().iterator();
		Entry<POIType, Drawable> element = null;
		Boolean stop=false;
		while((it.hasNext())&&(!stop)) {
			element=it.next();
			if (element.getKey().getName().equalsIgnoreCase(typename)) {
				stop=true;
			}
		}
		if (stop) {
			return element.getValue();
		} else {
			return null;
		}
	}
}