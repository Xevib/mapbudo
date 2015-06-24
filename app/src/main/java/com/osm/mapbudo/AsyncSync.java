package com.osm.mapbudo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

public class AsyncSync extends AsyncTask< HashMap<String,List<POI>>,Object,Pair<Integer,Integer>> {
    private Context c;
    private List<POI> points;
    private String username;
    private String password;
    private List<Pair<Long, Long>> id_conversion;

	public void setAuth(String username,String password){
		this.username=username;
		this.password=password;
	}
	public void setContext(Context c){
		this.c=c;
	}
	
	@Override
	protected Pair<Integer,Integer> doInBackground(HashMap<String,List<POI>>... params) {
		try{
			HashMap<String,List<POI>> outdated_pois=(HashMap<String,List<POI>>)params[0];

			//Open changeset
			HttpClient httpclient = new DefaultHttpClient();
			String url_create="http://api.openstreetmap.org/api/0.6/changeset/create";
			HttpPut request_create=new HttpPut(url_create);
			StringEntity entity=new StringEntity("<osm><changeset><tag k=\"created_by\" v=\"Mapbudo\"/><tag k=\"comment\" v=\"add POI\"/></changeset></osm>");

			entity.setContentType("text/xml");
			request_create.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(this.username,this.password),"UTF-8", false));
			request_create.setEntity(entity);
	 		HttpResponse response_create = httpclient.execute(request_create);
	
	 		if (response_create.getStatusLine().getStatusCode()==200)
	 		{	
	 			Integer changesetId = Integer.parseInt( EntityUtils.toString(response_create.getEntity(),HTTP.UTF_8) );
	 				
	 			//Upload POIs
	 			//Generate XML
	 			String xml="<osmChange generator=\"Mapbudo\">";
	 			if (outdated_pois.get("created").size()>0)
	 			{
	 				xml=xml.concat("<create>");
	 				for (int i=0;i<outdated_pois.get("created").size();i++)
		 			{
		 				xml=xml.concat(outdated_pois.get("created").get(i).getXMLCreate( changesetId.toString()) );
	
		 			}
	 				xml=xml.concat("</create>");
	 			}
	 			if (outdated_pois.get("modified").size()>0)
	 			{
	 				xml=xml.concat("<modify>");
	 				for (int i=0;i<outdated_pois.get("modified").size();i++)
		 			{
		 				xml=xml.concat(outdated_pois.get("modified").get(i).getXMLModify( changesetId.toString()) );
	
		 			}
	 				xml=xml.concat("</modify>");
	 			}
	 			if (outdated_pois.get("deleted").size()>0)
	 			{
	 				xml=xml.concat("<delete>");
	 				for (int i=0;i<outdated_pois.get("deleted").size();i++)
		 			{
		 				xml=xml.concat(outdated_pois.get("deleted").get(i).getXMLDelete( changesetId.toString()) );
		 			}
	 				xml=xml.concat("</delete>");
	 			}
	 			xml=xml.concat("</osmChange>");
	 			
	 			String url_upload="http://api.openstreetmap.org/api/0.6/changeset/"+String.valueOf(changesetId)+"/upload";
 				HttpPost request_upload=new HttpPost(url_upload);
	 			List<NameValuePair> parameters_upload = new LinkedList<NameValuePair>();
	 			parameters_upload.add(new BasicNameValuePair("id",changesetId.toString()));
	 			StringEntity entity_upload=new StringEntity(xml);
	
	 			entity_upload.setContentType("txt/xml");
	 			request_upload.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(this.username,this.password),"UTF-8", false));
	 			request_upload.setEntity(entity_upload);
	 			HttpResponse response_upload=httpclient.execute(request_upload);
	 			String xml_response=EntityUtils.toString(response_upload.getEntity(),HTTP.UTF_8);
	 			Log.v("response", xml_response);
	 			if (response_upload.getStatusLine().getStatusCode()==200)
                {
                    if (xml_response == "Element node/ has duplicate tags with key amenity") {
                        Log.e("wrong data", xml_response);
                        return new Pair<Integer, Integer>(1, changesetId);
                    }else if(xml_response.startsWith("Version mismatch: Provided")){
                        Log.v("Error","version error");
                    } else {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder;
                        builder = factory.newDocumentBuilder();
                        Document document = builder.parse(new InputSource(new StringReader(xml_response)));
                        XPathFactory xPathfactory = XPathFactory.newInstance();
                        XPath xpath = xPathfactory.newXPath();
                        XPathExpression expr_old = xpath.compile("/diffResult/node[@new_id  and @old_id]");
                        NodeList n_old = (NodeList) expr_old.evaluate(document, XPathConstants.NODESET);
                        this.id_conversion = new ArrayList<Pair<Long, Long>>();
                        this.points = new ArrayList<POI>();
                        this.points.addAll(outdated_pois.get("created"));
                        this.points.addAll(outdated_pois.get("modified"));
                        this.points.addAll(outdated_pois.get("deleted"));
                        int y;
                        POI tmp;
                        for (int x = 0; x < n_old.getLength(); x++) {
                            id_conversion.add(new Pair<Long, Long>(Long.valueOf(n_old.item(x).getAttributes().getNamedItem("old_id").getNodeValue()), Long.valueOf(n_old.item(x).getAttributes().getNamedItem("new_id").getNodeValue())));
                            y = 0;
                            if (points.get(x).getId()!=null) {
                                while (y < points.size() && ((-1 * points.get(y).getId()) != Long.valueOf(n_old.item(x).getAttributes().getNamedItem("old_id").getNodeValue()))) {
                                    y++;
                                }
                                if ((-1 * points.get(y).getId()) == Long.valueOf(n_old.item(x).getAttributes().getNamedItem("old_id").getNodeValue())) {
                                    tmp = points.get(y);
                                    tmp.setOsmId(Long.valueOf(n_old.item(x).getAttributes().getNamedItem("new_id").getNodeValue()));
                                    points.set(y, tmp);
                                }
                            }
                        }

                        //Close changeset
                        String url_close = "http://api.openstreetmap.org/api/0.6/changeset/" + String.valueOf(changesetId) + "/close";
                        List<NameValuePair> parameters_close = new LinkedList<NameValuePair>();
                        parameters_close.add(new BasicNameValuePair("id", changesetId.toString()));
                        HttpPut request_close = new HttpPut(url_close);
                        request_close.setEntity(new UrlEncodedFormEntity(parameters_close));
                        entity.setContentType("text/xml");
                        request_close.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(this.username, this.password), "UTF-8", false));
                        request_close.setEntity(entity);
                        HttpResponse response_close = httpclient.execute(request_close);
                        HttpEntity entity_close = response_close.getEntity();
                        String responseString = EntityUtils.toString(entity_close, "UTF-8");
                        Log.v("response_close", responseString);

                        if (response_close.getStatusLine().getStatusCode() == 200) {
                            return new Pair<Integer, Integer>(3, changesetId);

                        } else {
                            return new Pair<Integer, Integer>(2, changesetId);

                        }
                    }
                }
	 			else
	 			{
	 				return new Pair<Integer,Integer>(1,changesetId);
	 			}
	 			
	 		}
	 		else
	 		{
	 			//Log.v("tag",String.valueOf( response_create.getStatusLine().getStatusCode()));
	 			return new Pair<Integer,Integer>(0,null);
	 		}	
		}catch(Exception e){
			Log.e("log_tag", "Error in http connection "+e.toString());
		}
		return null;
	}
	@Override
	protected void onPostExecute(Pair<Integer,Integer> result)
	{
		BD bd=new BD(this.c);
		switch(result.first)
		{
			case 1:
				bd.openChangeset(result.second);
				break;
			case 2:
				bd.openChangeset(result.second);
				bd.uploadChangeset(result.second, points);
				break;
			case 3:
				bd.openChangeset(result.second);
				bd.uploadChangeset(result.second, points);
				bd.closeChangeset(result.second);
 				bd.changeIds(points,"OK");
 				((MainActivity)this.c).updateIds(points);
				break;
		}		
		bd.close();
	}
}
