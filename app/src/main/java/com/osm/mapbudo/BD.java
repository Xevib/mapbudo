package com.osm.mapbudo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
@SuppressLint("SimpleDateFormat")
public class BD{
	private SQLiteDatabase database;
	//Database managment
	public BD(Context c)
	{
		BDHelper dbHelper = new BDHelper(c);  
	    database = dbHelper.getWritableDatabase();
	}	
	public void close()
	{
		database.close();
	}
	
	
	
	
	
	//POI
	public void addPOI(POI p,String status)
	{
		ContentValues values = new ContentValues();
		values.put("id",p.getId());
		values.put("osmId",p.getOsmId());
		values.put("lat", p.getGeoPoint().getLatitude());
		values.put("lon", p.getGeoPoint().getLongitude());
		values.put("version", p.getVersion());
		values.put("status", status);
		values.put("uploaded", 0);
		String whereClause ;
		database.insert("poi", null, values);
		String []whereArgs;
		if (p.getOsmId()==null)
		{
			whereClause="id =?";
			whereArgs = new String[] {String.valueOf(p.getId())};
		}
		else
		{
			whereClause="osmId =?";
			whereArgs = new String[] {String.valueOf(p.getOsmId())};			
		}
		database.delete("value", whereClause , whereArgs);
		
		ContentValues values_tags;
		Iterator<Entry<String, String>> it=p.getValues().entrySet().iterator();
		Entry<String,String> pair;
		while (it.hasNext())
		{
			pair=it.next();
			values_tags = new ContentValues();
			values_tags.put("key",pair.getKey());
			values_tags.put("value",pair.getValue());
			if (p.getOsmId()==null)
			{
				values_tags.put("osmId", 0);
				values_tags.put("id",p.getId());
				
			}
			else
			{
				values_tags.put("osmId", p.getOsmId());
				values_tags.put("id", 0);
			}
			database.insert("value", null, values_tags);
		}
		
		if (p.getType()!=null)
		{
			it=p.getType().getHeredated().entrySet().iterator();
			
			while (it.hasNext())
			{
				pair=it.next();
				values_tags = new ContentValues();
				values_tags.put("key",pair.getKey());
				values_tags.put("value",pair.getValue());
				if (p.getOsmId()==null)
				{
					values_tags.put("osmId", 0);
					values_tags.put("id",p.getId());
				}
				else
				{
					values_tags.put("osmId", p.getOsmId());
					values_tags.put("id", 0);
				}
				database.insert("value", null, values_tags);
			}
		}
		
	}
	public POI getPOI(Long id,List<POIType>avaible_types)
	{
		String []columns=new String[]{"lat","lon","version"};
		Cursor cur=database.query("poi", columns, "osmId="+String.valueOf(id), null, null, null, null);
		if (cur.getCount()==0)
		{
			cur.close();
			Cursor cur_id=database.query("poi", columns, "id="+String.valueOf(id), null, null, null, null);
			if (cur_id.getCount()==0)
			{
				return null;
			}
			else
			{
				cur_id.moveToFirst();
				Float lat=cur_id.getFloat(cur.getColumnIndex("lat"));
				Float lon=cur_id.getFloat(cur.getColumnIndex("lon"));
				Integer version=cur_id.getInt(cur.getColumnIndex("version"));
				cur_id.close();
				String []columns_tags=new String[]{"key" , "value" };
				Cursor cur_tags=database.query("value", columns_tags, "osmId="+String.valueOf(id), null, null, null, null);
				HashMap<String,String> values=new HashMap<String,String>();
				cur_tags.moveToFirst();
				while(!cur_tags.isAfterLast())
				{
					values.put(cur_tags.getString(cur_tags.getColumnIndex("key")), cur_tags.getString(cur_tags.getColumnIndex("value")));
					cur_tags.moveToNext();
				}
				cur_tags.close();
				POI p=new POI(lat, lon, values, id, null,null,version);
				p.detect_type(avaible_types);
				return p;
			}
		}
		else
		{
			cur.moveToFirst();
			Float lat=cur.getFloat(cur.getColumnIndex("lat"));
			Float lon=cur.getFloat(cur.getColumnIndex("lon"));
			Integer version=cur.getInt(cur.getColumnIndex("version"));
			cur.close();
			String []columns_tags=new String[]{"key" , "value" };
			Cursor cur_tags=database.query("value", columns_tags, "osmId="+String.valueOf(id), null, null, null, null);
			HashMap<String,String> values=new HashMap<String,String>();
			cur_tags.moveToFirst();
			while(!cur_tags.isAfterLast())
			{
				values.put(cur_tags.getString(cur_tags.getColumnIndex("key")), cur_tags.getString(cur_tags.getColumnIndex("value")));
				cur_tags.moveToNext();
			}
			cur_tags.close();
			
			POI p= new POI(lat, lon, values, null, id,null,version);
			p.detect_type(avaible_types);
			return p;
		}
	}
	public void deletePOI(POI p)
	{
		ContentValues values = new ContentValues();
		values.put("status", "deleted");
		values.put("uploaded", 0);
		if (p.getId()==null)
		{
			database.update("poi", values, "osmId=", new String[]{String.valueOf(p.getOsmId())});
		}
		else
		{
			database.update("poi", values, "osmId=", new String[]{String.valueOf(p.getId())});
		}
		
	}
	
	public void updatePOI(POI p)
	{ 
		String [] whereArgs;
		if (p.getOsmId()==null)
		{
			
			ContentValues values = new ContentValues();
			values.put("osmId",p.getOsmId());
			values.put("id", p.getId());
			values.put("lat", p.getGeoPoint().getLatitude());
			values.put("lon", p.getGeoPoint().getLongitude());
			values.put("version", p.getVersion());
			values.put("status", "modified");
			values.put("uploaded",0);
			
			whereArgs = new String[] {String.valueOf(p.getId())};
			
			database.update("poi", values, "id=?", whereArgs);
			database.delete("value", "id=?" , whereArgs);
			
			ContentValues values_tags;
			Iterator<Entry<String, String>> it=p.getValues().entrySet().iterator();
			Entry<String,String> pair;
			while (it.hasNext())
			{
				pair=it.next();
				values_tags = new ContentValues();
				if (p.getOsmId()==null)
				{
					values_tags.put("osmId", 0);
					values_tags.put("id",p.getId());
				}
				else
				{
					values_tags.put("osmid", p.getOsmId());
					values_tags.put("id", 0);
				}
				values.put("key", pair.getKey());
				values.put("value",pair.getValue());
				database.insert("poi", null, values);
			}
			
			
		}
		else
		{
			ContentValues values = new ContentValues();
			values.put("osmId",p.getOsmId());
			values.put("lat", p.getGeoPoint().getLatitude());
			values.put("lon", p.getGeoPoint().getLongitude());
			values.put("version", p.getVersion());
			values.put("status", "modified");
			
			whereArgs = new String[] {String.valueOf(p.getOsmId())};
			Integer numr=database.update("poi",values, "osmId=?" , whereArgs);
			database.delete("value", "osmId=?" , whereArgs);
			
			ContentValues values_tags;
			Iterator<Entry<String, String>> it=p.getValues().entrySet().iterator();
			Entry<String,String> pair;
			while (it.hasNext())
			{
				pair=it.next();
				values_tags = new ContentValues();
				if (p.getOsmId()==null)
				{
					values_tags.put("osmId", 0);
					values_tags.put("id",p.getId());
				}
				else
				{
					values_tags.put("osmid", p.getOsmId());
					values_tags.put("id", 0);
				}
				values_tags.put("key", pair.getKey());
				values_tags.put("value",pair.getValue());
				database.insert("value", null, values_tags);
			}
			
			
			
		}
	}
	public void setMarkPOIsUploaded()
	{
		ContentValues value=new ContentValues();
		value.put("uploaded", 1);
		database.update("poi",value , "s", null);
	}
	public HashMap<String,List<POI>> getNotUploadedPOIs(List<POIType> avaible_types)
	{
		HashMap<String,List<POI>>outdated=new HashMap<String,List<POI>>();
		
		List<POI> created=new ArrayList<POI>();
		List<POI> modified=new ArrayList<POI>();
		List<POI> deleted=new ArrayList<POI>();
		
		String []columns=new String[]{"id","osmId","lat","lon","version"};
		Cursor cur=database.query("poi", columns, "status=='created'", null, null, null, null);
		if (cur.getCount()!=0)
		{
			POI p;
			cur.moveToFirst();
			Float lat,lon;
			Long osmId,id;
			Integer version;
			lat=null;lon=null;
			osmId=null;id=null;
			version=null;
			while (!cur.isAfterLast())
			{
				lat=cur.getFloat(cur.getColumnIndex("lat"));
				lon=cur.getFloat(cur.getColumnIndex("lon"));
				id=cur.getLong(cur.getColumnIndex("id"));
				osmId=cur.getLong(cur.getColumnIndex("osmId"));				
				version=cur.getInt(cur.getColumnIndex("version"));
				String []columns_tags=new String[]{"key" , "value" };
				if ((id==null)||(id==0))
				{
					Cursor cur_tags=database.query("value", columns_tags, "osmId="+String.valueOf(osmId), null, null, null, null);
					HashMap<String,String> values=new HashMap<String,String>();
					cur_tags.moveToFirst();
					while(!cur_tags.isAfterLast())
					{
						values.put(cur_tags.getString(cur_tags.getColumnIndex("key")), cur_tags.getString(cur_tags.getColumnIndex("value")));
						cur_tags.moveToNext();
					}
					cur_tags.close();
					p=new POI(lat, lon, values, null, osmId,null,version);
				}
				else
				{
					Cursor cur_tags=database.query("value", columns_tags, "id="+String.valueOf(id), null, null, null, null);						HashMap<String,String> values=new HashMap<String,String>();
					cur_tags.moveToFirst();
					while(!cur_tags.isAfterLast())
					{
						values.put(cur_tags.getString(cur_tags.getColumnIndex("key")), cur_tags.getString(cur_tags.getColumnIndex("value")));
						cur_tags.moveToNext();
					}
					cur_tags.close();
					p=new POI(lat, lon, values, id, null,null,version);
				}
				p.setStatus("created");
				p.detect_type(avaible_types);
				created.add(p);
				cur.moveToNext();
			}
		}
		cur=database.query("poi", columns, "status=='modified'", null, null, null, null);
		if (cur.getCount()!=0)
		{
			POI p;
			cur.moveToFirst();
			Float lat,lon;
			Long osmId,id;
			int version;
			lat=null;lon=null;
			osmId=null;id=null;
			while (!cur.isAfterLast())
			{
				lat=cur.getFloat(cur.getColumnIndex("lat"));
				lon=cur.getFloat(cur.getColumnIndex("lon"));
				id=cur.getLong(cur.getColumnIndex("id"));
				version=cur.getInt( cur.getColumnIndex("version"));
				osmId=cur.getLong(cur.getColumnIndex("osmId"));				
					
				String []columns_tags=new String[]{"key" , "value" };
				if ((id==null)|| (id==0))
				{
					Cursor cur_tags=database.query("value", columns_tags, "osmId="+String.valueOf(osmId), null, null, null, null);
					HashMap<String,String> values=new HashMap<String,String>();
					cur_tags.moveToFirst();
					while(!cur_tags.isAfterLast())
					{
						values.put(cur_tags.getString(cur_tags.getColumnIndex("key")), cur_tags.getString(cur_tags.getColumnIndex("value")));
						cur_tags.moveToNext();
					}
					cur_tags.close();
					p=new POI(lat, lon, values, null, osmId,null,version);
				}
				else
				{
					Cursor cur_tags=database.query("value", columns_tags, "id="+String.valueOf(id), null, null, null, null);						HashMap<String,String> values=new HashMap<String,String>();
					cur_tags.moveToFirst();
					while(!cur_tags.isAfterLast())
					{
						values.put(cur_tags.getString(cur_tags.getColumnIndex("key")), cur_tags.getString(cur_tags.getColumnIndex("value")));
						cur_tags.moveToNext();
					}
					cur_tags.close();
					p=new POI(lat, lon, values, id, null,null,version);
				}
				p.setStatus("modified");
				p.detect_type(avaible_types);
				modified.add(p);
				cur.moveToNext();
			}
		}
		cur=database.query("poi", columns, "status=='deleted'", null, null, null, null);
		if (cur.getCount()!=0)
		{
			POI p;
			cur.moveToFirst();
			Float lat,lon;
			Long osmId,id;
			Integer version;
			lat=null;lon=null;
			osmId=null;id=null;
			;
			while (!cur.isAfterLast())
			{
				lat=cur.getFloat(cur.getColumnIndex("lat"));
				lon=cur.getFloat(cur.getColumnIndex("lon"));
				id=cur.getLong(cur.getColumnIndex("id"));
				osmId=cur.getLong(cur.getColumnIndex("osmId"));
				version=cur.getInt(cur.getColumnIndex("version"));
					
				String []columns_tags=new String[]{"key" , "value" };
				if ((id==null) || (id==0))
				{
					Cursor cur_tags=database.query("value", columns_tags, "osmId="+String.valueOf(osmId), null, null, null, null);
					HashMap<String,String> values=new HashMap<String,String>();
					cur_tags.moveToFirst();
					while(!cur_tags.isAfterLast())
					{
						values.put(cur_tags.getString(cur_tags.getColumnIndex("key")), cur_tags.getString(cur_tags.getColumnIndex("value")));
						cur_tags.moveToNext();
					}
					cur_tags.close();
					p=new POI(lat, lon, values, null, osmId,null,version);
				}
				else
				{
					Cursor cur_tags=database.query("value", columns_tags, "id="+String.valueOf(id), null, null, null, null);
					HashMap<String,String> values=new HashMap<String,String>();
					cur_tags.moveToFirst();
					while(!cur_tags.isAfterLast())
					{
						values.put(cur_tags.getString(cur_tags.getColumnIndex("key")), cur_tags.getString(cur_tags.getColumnIndex("value")));
						cur_tags.moveToNext();
					}
					cur_tags.close();
					p=new POI(lat, lon, values, id, null,null,version);
				}
				p.setStatus("deleted");
				p.detect_type(avaible_types);
				deleted.add(p);
				cur.moveToNext();
			}
		}
		outdated.put("created", created);
		outdated.put("modified", modified);
		outdated.put("deleted", deleted);
		return outdated;
	}	
	public Long getFreeId() {
		
		Long ret=(long)1;
		Cursor cur=database.query("poi", new String [] {"MAX(id)"}, null, null, null, null, null);
		cur.moveToFirst();
		if (cur.getCount()!=0)
		{
			ret=(cur.getLong(0)+1);
		}
		return ret;
	}
	
	//CHANGESET
	public void openChangeset(Integer changesetId)
	{
		ContentValues values = new ContentValues();
		values.put("changesetId",changesetId);
		values.put("status","open");
		
		database.insert("changeset", null, values);
	}
	public void uploadChangeset(Integer changesetId,List<POI> points)
	{
	
		ContentValues values=new ContentValues();
		values.put("status","uploaded");
		database.update("changeset", values, "changesetId=?",new String[]{String.valueOf(changesetId)});
		Iterator<POI> it=points.iterator();
		
		ContentValues values_point=new ContentValues();
		values_point.put("changeset", changesetId);
		POI p;
		while (it.hasNext())
		{
			p=it.next();
			database.update("poi",values_point,"osmId=?",new String[]{String.valueOf(p.getOsmId())});
		}
		
	}
	public void closeChangeset(Integer changesetId)
	{
		ContentValues values = new ContentValues();
		values.put("status","open");
		database.update("changeset",values,"changesetId=?",new String[]{String.valueOf(changesetId)});
	}
	public void changeIds(List<POI> outdated_pois, String status) {
		for (POI p:outdated_pois)
		{
			String [] whereArgs;
			ContentValues values = new ContentValues();
			values.put("id",0);
			values.put("osmId",p.getOsmId());
			values.put("lat", p.getGeoPoint().getLatitude());
			values.put("lon", p.getGeoPoint().getLongitude());
			values.put("status", status);
			values.put("uploaded",0);
				
			whereArgs = new String[] {String.valueOf(p.getId())};
			database.update("poi", values, "id=?", whereArgs);
			
			ContentValues values_tags = new ContentValues();
			values_tags.put("osmId",p.getOsmId());
			database.update("value", values_tags, "id=?", whereArgs);
											
		}
	}
	public void markdeleteId(Long id) {
		ContentValues values = new ContentValues();
		values.put("status", "deleted");
		values.put("uploaded", 0);
		
		database.update("poi", values, "osmId=?", new String[]{String.valueOf(id)});
	}

		
}
	
