package com.osm.mapbudo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Node;

public class POI {

	private Long id;
	private Long osmId;
	private HashMap<String,String> values;
	private double lon;
	private double lat;
	private POIType type;
	private String status;
	private int version;
	public void setVersion(Integer version)
	{
		this.version=version;
	}
	public Integer getVersion()
	{
			return this.version;
	}
	public POI(double lat,double lon,HashMap<String,String> values,Long id,Long osmId,POIType type,Integer version)
	{
		this.version=version;
		this.type=type;
		this.id=id;
		this.osmId=osmId;
		this.lat=lat;
		this.lon=lon;
		if (values==null){
			this.values=new HashMap<String,String>();
		} else {
			this.values=values;
		}
	}
	public POI(Node item) {
		this.osmId=Long.parseLong(item.getAttributes().getNamedItem("id").getNodeValue());
		this.lat=Double.parseDouble(item.getAttributes().getNamedItem("lat").getNodeValue());
		this.lon=Double.parseDouble(item.getAttributes().getNamedItem("lon").getNodeValue());
		this.version=Integer.parseInt(item.getAttributes().getNamedItem("version").getNodeValue());
		values=new HashMap<String,String>();
		for(int x=1;x<(item.getChildNodes().getLength()-1);x+=2) {
			values.put(item.getChildNodes().item(x).getAttributes().getNamedItem("k").getNodeValue(), item.getChildNodes().item(x).getAttributes().getNamedItem("v").getNodeValue());
		}
	}
	public Long getId()
	{
		return this.id;
	}
	public Long getOsmId()
	{
		return this.osmId;
	}
	public void setId(Long id)
	{
		this.id=id;
	}
	public void setOsmId(Long osmId)
	{
		this.osmId=osmId;
	}
	public GeoPoint getGeoPoint()
	{
		return new GeoPoint(this.lat, this.lon);
	}
	public void addValue(String key,String value )
	{
		this.values.put(key, value);
	}
	public String getValue(String key)
	{
		return this.values.get(key);
	}
	public POIType getType()
	{
		return this.type;
	}
	public void setType(POIType type)
	{
		this.type=type;
	}
	public String getXMLDelete(String changeset)
	{
		return "<node id='"+this.osmId+"' version='"+this.getVersion().toString()+"' changeset='"+changeset+"' lat='"+this.lat+"' lon='"+this.lon+"'/>";
	}
	public String getXMLCreate(String changeset) {
		String xml;
		if (this.id==null){
			xml="<node id='"+this.osmId+"' changeset='"+changeset+"' lat='"+this.lat+"' lon='"+this.lon+"'>";
		} else {
			xml="<node id='-"+this.id+"' changeset='"+changeset+"' lat='"+this.lat+"' lon='"+this.lon+"'>";
		}
        Iterator<Entry<String, String>> it2= this.type.getHeredated().entrySet().iterator();
        Entry<String,String> pair;
        while (it2.hasNext()) {
            pair=it2.next();
            xml=xml.concat("<tag k='"+ pair.getKey()+"' v='"+pair.getValue()+"'/>");
        }


        Field f;
        Iterator<Field> it= this.type.getFields().iterator();
		while (it.hasNext()) {
            f= it.next();
            xml=xml.concat(f.getXML());
		}
		xml=xml.concat("</node>");
		return xml;
	}
	public String getXMLModify(String changeset) {
		String xml="<node id='"+this.osmId+"' version='"+this.version+"' changeset='"+changeset+"' lat='"+this.lat+"' lon='"+this.lon+"'>";

        Iterator<Entry<String, String>> it2= this.type.getHeredated().entrySet().iterator();
        Entry<String,String> pair;
        while (it2.hasNext()) {
            pair=it2.next();
            xml=xml.concat("<tag k='"+ pair.getKey()+"' v='"+pair.getValue()+"'/>");
        }

        Iterator<Field> it = this.type.getFields().iterator();
        Field f;
		while (it.hasNext()) {
            f= it.next();
			xml=xml.concat(f.getXML());
		}
		xml=xml.concat("</node>");
		return xml;
	}
	public HashMap<String,String> getValues()
	{
		return this.values;
	}
	
	public void setGeoPoint(GeoPoint position) {
		this.lat=position.getLatitude();
		this.lon=position.getLongitude();	
	}
	public String getStatus()
	{
		return this.status;
	}
	public void setStatus(String status)
	{
		this.status=status;
	}
	public void detect_type(List<POIType> avaible_types)
	{
		int x=0;
		while (x<avaible_types.size()&&(!avaible_types.get(x).match(this.values))){
			x++;
		}
		if(x<avaible_types.size()) {
			this.type=avaible_types.get(x);
		} else {
			this.type=null;
		}
	}
	public void setTags(HashMap<String, String> values) {
		this.values=values;
		
	}
}
