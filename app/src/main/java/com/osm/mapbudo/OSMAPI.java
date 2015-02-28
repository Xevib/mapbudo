package com.osm.mapbudo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

public class OSMAPI {
	Context context;
    List <Pair<Long,Long>> ids_conversion;
	public OSMAPI(Context context)
	{
		this.context=context;
		
	}
	public Boolean getUserDetails(String username, String password)
	{
		AsyncTask<String, Object, Boolean> asyncGetDetails=new AsyncTask<String, Object, Boolean>()
		{

			@Override
			protected Boolean doInBackground(String... params) {
				try{
					HttpClient httpclient = new DefaultHttpClient();
			        HttpGet request = new HttpGet("http://api.openstreetmap.org/api/0.6/user/details");
					request.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(params[0], params[1]),"UTF-8", false));
		 			HttpResponse response = httpclient.execute(request);
		 			if (response.getStatusLine().getStatusCode()==200)
		 			{
		 				String resposta = EntityUtils.toString(response.getEntity());
		 				//Al string hi ha la resposta
		 				Log.v("tag",resposta);
		 				return true;
		 			}
		 			else
		 			{
		 				
		 				Log.v("tag",String.valueOf( response.getStatusLine().getStatusCode()));
		 				return false;
		 			}
		 			
			       }catch(Exception e){
			           Log.e("log_tag", "Error in http connection "+e.toString());
			           return false;
			       }
			}};
		asyncGetDetails.execute(username,password);
		try {
			return asyncGetDetails.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
			
	}
	public void getData(List< Pair<String,String>> tags,double east,double north,double south,double  west,final List<POIType>avaible_types,Integer zoomLevel)
	{
		//http://overpass-api.de/api/interpreter?data=(%0A%20%20node%0A%20%20%20%20%5B%22amenity%22%3D%22fire_station%22%5D%0A%20%20%20%20(50.6%2C7.0%2C50.8%2C7.3)%3B%0A%20%20node%0A%20%20%20%20%5B%22amenity%22%3D%22bank%22%5D%0A%20%20%20%20(50.6%2C7.0%2C50.8%2C7.3)%3B%0A)%3B%0A(._%3B%3E%3B)%3B%0Aout%3B
		//(
		//		  node
		//		    ["amenity"="fire_station"]
		//		    (50.6,7.0,50.8,7.3);
		//		  node
		//		    ["amenity"="bank"]
		//		    (50.6,7.0,50.8,7.3);
		//		);
		//		(._;>;);
		//		out;
		if ( !((MainActivity)this.context).active_get  )
		{	
			AsyncGetData async=new AsyncGetData();
			List<Pair<String,String>> zl=new ArrayList<Pair<String,String>> ();
			zl.add(new Pair(zoomLevel.toString(),null));
			List<Pair<String,String>> bbox= new ArrayList<Pair<String,String>>();
			bbox.add(new Pair<String,String>("east",String.valueOf(east) ));
			bbox.add(new Pair<String,String>("north",String.valueOf(north)));
			bbox.add(new Pair<String,String>("south",String.valueOf(south)));
			bbox.add(new Pair<String,String>("west",String.valueOf(west)));
			async.setContext(this.context);
			async.setPOITypes(avaible_types);
			((MainActivity)this.context).active_get=true;;
			async.execute(tags,bbox,zl);
		}
		
		
	}

	public void addPOI(List<POIType> avaible_types)
	{

		BD bd =new BD(context);
		Log.v("bd", "open");
		HashMap<String,List<POI>> outdated=bd.getNotUploadedPOIs(avaible_types);
		Log.v("bd","close");
		bd.close();
		AsyncSync asyncAddPOI=new AsyncSync();
		asyncAddPOI.setContext(this.context);
		SharedPreferences preferences =  this.context.getSharedPreferences("com.osm.budomap", Context.MODE_PRIVATE);
		String user=preferences.getString("user", null);
		String password=preferences.getString("password", null);
		if ((user!=null)&& (password!=null))
		{
			asyncAddPOI.setAuth(user, password);
			asyncAddPOI.execute(outdated);

		}
		else
		{
			Toast.makeText(this.context,this.context.getResources().getString(R.string.password_not_set), Toast.LENGTH_LONG).show();
		}
		
	}
	
	
}
