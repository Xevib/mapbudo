package com.osm.mapbudo;

import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

public class AsyncGetData extends AsyncTask<List<Pair<String,String>>, Object, List<POI>> {
	List<POI> points;
	Context context;
	List<POIType> avaible_types;
	@Override
	protected List<POI> doInBackground(List<Pair<String,String>>... params) {
		try{
			points=new ArrayList<POI>();
			HttpClient httpclient = new DefaultHttpClient();
			String url="http://overpass-api.de/api/interpreter?data=";
			String query="(";
					//(
					//		  node
					//		    ["amenity"="fire_station"]
					//		    (50.6,7.0,50.8,7.3);
					//		  node
					//		    ["amenity"="bank"]
					//		    (50.6,7.0,50.8,7.3);
					//		);
					//		(._;>;);
					//		out meta;
			for(Pair<String,String>  pair:params[0])
			{
				query=query+"node[\""+pair.first+"\"=\""+pair.second+"\"]("+params[1].get(0).second+","+params[1].get(1).second+","+params[1].get(2).second+","+params[1].get(3).second+");";
			}
			query=query+");(._;>;);out meta;";
			Integer zoomlevel=Integer.valueOf(params[2].get(0).first);
			if (zoomlevel>15)
			{
				//String url="http://api.openstreetmap.org/api/0.6/map?bbox="+params[1].get(1).second+","+params[1].get(0).second+","+params[1].get(3).second+","+params[1].get(2).second+"";
				Log.v("overpass url", url);

				if (params[0]!=null)
				{
					Log.v("url",url+query);
					HttpGet request = new HttpGet(  url+URLEncoder.encode(query));
					HttpResponse response = httpclient.execute(request);
					if (response.getStatusLine().getStatusCode()==200)
					{
						
						String resposta = EntityUtils.toString(response.getEntity(),HTTP.UTF_8);	
						InputSource inSource =  new InputSource(new StringReader(resposta));
						//XMLPullParserHandler parser = new XMLPullParserHandler();
				        //points = parser.parse(stream,avaible_types);
					    XPathFactory factory = XPathFactory.newInstance();
					    XPath xPath = factory.newXPath();
					    NodeList poi_nodes = (NodeList) xPath.evaluate("//osm/node",inSource , XPathConstants.NODESET);
					    for (int x=0;x<poi_nodes.getLength();x++)
					    {
					    	points.add(new POI(poi_nodes.item(x)));
					    }
					 	return points;
					 }
					 else
					 {
						 Log.v("tag",String.valueOf( response.getStatusLine().getStatusCode()));
						 return null;
					 }
				}
			}
			else
			{
				return points;
			}
		}
		catch(Exception e){
			Log.e("log_tag", "Error in http connection "+e.toString());
		}
		return null;
	}
	@Override
	protected void onPostExecute(List<POI> result) {
		
		
		Log.v("bd", "open");
		BD bd=new BD(this.context);
		if (result!=null)
		{
			for (POI point:result)
			{
				point.detect_type(avaible_types);
				if (point.getType()!=null)
				{
					bd.addPOI(point, "OK");
					((MainActivity)this.context).addPOI(point);
				}
			}
		}
		Log.v("bd","close");
		bd.close();
		((MainActivity)this.context).active_get=false;
	}
	public void setContext(Context c)
	{
		this.context=c;
	}
	public void setPOITypes(List<POIType> avaible_types)
	{
		this.avaible_types=avaible_types;
	}
	
}
