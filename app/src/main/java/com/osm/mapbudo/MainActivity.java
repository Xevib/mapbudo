package com.osm.mapbudo;

//TODO He trobat molt a faltar un «marker» quan vas a crear un node. Encara que sigui una cosa senzilla[1], més que res per saber on afegiràs informació.
//TODO El menú de l'esquerra, amb les etiquetes mostra molta informació. Hi ha moltes etiquetes i això mola. També és veritat, però, que en falten moltes... I jo pensava...
//TODO No molaria tenir un espai on poder dir quines categories vols que apareguin al menú i quines no? Per exemple, potser jo sé que no entraré info de transports, llavors amago aquella categoria
//TODO O encara més... Molaria molt que poguessis escollir perfils. Per exemple, perfil «Muntanya» amb etiquetes del tipus de nodes que podries trobar allà, o perfil «Ciutat» o altres...
//TODO Etiquetes com «Opening_hours» haurien de disposar d'algun tipus d'ajuda. Un desplegable amb exemples o quelcom similar. Jo no tinc pebrots d'emplenar aquesta etiqueta sense una xuleta.
//TODO Per etiquetes amb molts valors (com «Services» o «Shoping») estaria molt bé plegar la categoria des del darrer valor de la categoria, ja que si no t'obliga a fer scroll fins a dalt de tot per poder tancar-la.
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.support.v4.view.GravityCompat;

public class MainActivity extends ActionBarActivity implements MapEventsReceiver,OnMarkerDragListener {
	private DrawerLayout drawerlayout;
	private ActionBarDrawerToggle drawertoggle;
	private ExpandableListView drawerlist;
	private boolean values_visible;
	private HashMap<String, String> newPOI_values;
	private double newPOI_lon;
	private double newPOI_lat;
	private POIType actualPOI_type;
	private LocationManager locationmanager;
	private Menu menu;
	BudoLocationListener bll;
	private double lastlat;
	private double lastlon;
	private int lastzoom;
	
	private Boolean mode_add_poi;
	private Filter filt;
	private Marker selected_marker;
	List<POIType> avaible_types;
	private Marker marker_drag;
	public Boolean active_get;

	@SuppressWarnings("serial")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.values_visible=false;
		this.active_get=false;
        this.mode_add_poi=false;

		menu=null;
		locationmanager = (LocationManager)this.getApplicationContext().getSystemService(LOCATION_SERVICE);
		bll=new BudoLocationListener();
		drawerlayout=(DrawerLayout) this.findViewById(R.id.drawer_layout);
		drawerlist=(ExpandableListView) this.findViewById(R.id.left_drawer);
		this.avaible_types= new ArrayList<POIType>();

        this.filt =this.initializeFiltter();


		ExpandableListAdapter ExpLstAdapter = new ExpandableListAdapter(this, filt);
		drawerlist.setAdapter(ExpLstAdapter);
		
		this.load_state();

		drawerlist.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int position, long id) {
				setNewPOIType(filt.getType(groupPosition,position));
				drawerlayout.closeDrawer(Gravity.LEFT);
				return false;
			}
		});

		drawertoggle=new ActionBarDrawerToggle(
				this,
				drawerlayout, 
				R.drawable.ic_navigation_drawer,
				R.string.options_open, 
				R.string.options_close)
			{
				@Override
				public void onDrawerClosed(View drawerView) {};
				@Override
				public void onDrawerOpened(View drawerView) {};
				
			};
		drawerlayout.setDrawerListener(drawertoggle);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragmentContainer()).commit();
			getSupportFragmentManager().beginTransaction().add(R.id.frame_values, new PlaceholderFragmentValue()).commit();
		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu_passed) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu=menu_passed;
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		if (drawertoggle.onOptionsItemSelected(item)){
			return true;
		}
		if (id == R.id.action_settings) {
			this.openSettingsActivity();
			return true;
		} else if(id==R.id.action_locate) {
			this.center_map_to_location();
			return true;
		} else if(id==R.id.action_delete_marker) {
			this.deleSelectedPOI();
			return true;
		}
		
		
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  
	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		SharedPreferences preferences = getSharedPreferences("com.osm.mapbudo", Context.MODE_PRIVATE);
		this.lastlat=Double.parseDouble(preferences.getString("lastlat","0"));
		this.lastlon=Double.parseDouble( preferences.getString("lastlon","0"));
		this.lastzoom=preferences.getInt("lastzoom", 5);
		MapView map=(MapView) findViewById(R.id.mapview);
		map.getController().setCenter(this.getLastMapCenter());
		map.getController().setZoom(this.getLastZoom());
		map.invalidate();
	}
	private void openSettingsActivity() {
		Intent intent = new Intent(this, Settings.class);
		this.startActivityForResult(intent, 2);
	
	}

	private void deleSelectedPOI() {
		Long id=Long.valueOf(this.selected_marker.getTitle());
		BD bd=new BD(this);
		bd.markdeleteId(id);
		bd.close();
		
		Boolean suc=((MapView)this.findViewById(R.id.mapview)).getOverlays().remove(this.selected_marker);
		OSMAPI api=new OSMAPI(this);
		api.addPOI(filt.getTypes());
		this.hideValues();
		((MapView)this.findViewById(R.id.mapview)).invalidate();
		
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawertoggle.syncState();
	};
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawertoggle.onConfigurationChanged(newConfig);
	}
	
	private void center_map_to_location() {
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,bll);
		Location location_network = locationmanager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location location_gps = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location location_passive = locationmanager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		Location location;
		if ((location_network!=null)&&(location_gps!=null)&&(location_network.getTime()>location_gps.getTime())&&(location_network.getTime()>location_passive.getTime() ))
		{
			location=location_network;
		}
		else if((location_gps!=null)&&(location_gps.getTime()>location_network.getTime())&&(location_gps.getTime()>location_passive.getTime()) )
		{
			location=location_gps;
		}
		else 
		{
			if (location_passive!=null)
			{
				location=location_passive;
			}
			else
			{
				location=null;
			}
		}
		if (location!=null)
		{
			double lat=location.getLatitude();
			double lon=location.getLongitude();
			GeoPoint gpoint=new GeoPoint(lat,lon,0);
			MapView map=(MapView)this.findViewById(R.id.mapview);
			map.getController().setCenter(gpoint);
			map.getController().setZoom(17);
			map.invalidate();
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragmentContainer extends Fragment {

		Handler handlerTimer;
		Runnable rRefresh;
		public PlaceholderFragmentContainer() {
		}
		@Override
		public void onResume()
		{
			super.onResume();
			((MainActivity)getActivity()).load_state();
			MapView map=(MapView) getActivity().findViewById(R.id.mapview);
			map.getController().setCenter(((MainActivity)getActivity()).getLastMapCenter());
			map.getController().setZoom(((MainActivity)getActivity()).getLastZoom());
			map.invalidate();
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
					View rootView = inflater.inflate(R.layout.fragment_main, container,false);
					MapView map=(MapView)rootView.findViewById(R.id.mapview);	
					MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(rootView.getContext(), (MapEventsReceiver) getActivity() );
					rRefresh=new Runnable(){
				        public void run() {
				        	((MainActivity)getActivity()).refreshPOIs();
				      }};

					map.setMapListener(new MapListener() {
						
						
						private long lastScroll=0;

						@Override
						public boolean onZoom(ZoomEvent event) {
							Integer lastzoom=((MainActivity)getActivity()).getLastZoom();
							//Detect zoom out
							if (event.getZoomLevel()<lastzoom)
							{
								((MainActivity)getActivity()).refreshPOIs();
							}
							((MainActivity)getActivity()).setLastZoom(event.getZoomLevel());
							
							return false;
						}
						
						@Override
						public boolean onScroll(ScrollEvent event) {
							MapView map=(MapView) getActivity().findViewById(R.id.mapview);
							if (map!=null)
							{
									if (  ( Math.abs(((MainActivity)getActivity()).getLastLat()-((event.getX()/1E6))) >(2/1E6))  ||( Math.abs(((MainActivity)getActivity()).getLastLon()-((event.getY()/1E6)))>(2/1E6)))
									{
										((MainActivity)getActivity()).setMainMenu();
										((MainActivity)getActivity()).unfollow();
										
										if (handlerTimer==null)
										{
											handlerTimer=new Handler();
										}
										if ((this.lastScroll+300)<System.currentTimeMillis())
										{
											handlerTimer.postDelayed(rRefresh, 300);
										}
										else
										{
											handlerTimer.removeCallbacks(rRefresh);
											handlerTimer.postDelayed(rRefresh, 300);
										}
										this.lastScroll=System.currentTimeMillis();
										((MainActivity)getActivity()).setLastLat(event.getX()/1E6);
										((MainActivity)getActivity()).setLastLon(event.getY()/1E6);
										
									}
							}
							return false;
						}

						
					});
					MapController mapController = (MapController) map.getController();
					map.setMaxZoomLevel(19);
			        mapController.setZoom(((MainActivity)this.getActivity()).getLastZoom());
					
			        map.setMultiTouchControls(true);
			        map.setBuiltInZoomControls(true);
			        //ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
			        map.getOverlays().add(mapEventsOverlay);

				return rootView;
		}
	}
	
	@Override
	public boolean singleTapConfirmedHelper(GeoPoint p) {
		
		
		return false;
	}

	public void load_state() {
		SharedPreferences preferences = getSharedPreferences("com.osm.mapbudo", Context.MODE_PRIVATE);
		this.lastlat=Double.parseDouble(preferences.getString("lastlat","0"));
		this.lastlon=Double.parseDouble( preferences.getString("lastlon","0"));
		this.lastzoom=preferences.getInt("lastzoom", 5);
		this.filt.load(preferences);
	}

	protected void setMenuItemSelected(POI p,Marker marker) {
		menu.clear();
		getMenuInflater().inflate(R.menu.item_selected, menu);
        this.actualPOI_type =p.getType();
		this.selected_marker=marker;

        for (Field f:actualPOI_type.getFields()) {

        }

        this.showValues();
		if (p.getValue("name")!=null) {
			//((EditText)this.findViewById(R.id.etName)).setText(p.getValue("name"));
		} else {
			//((EditText)this.findViewById(R.id.etName)).setText("");
		}
		if(p.getValue("address")!=null) {
			//((EditText)this.findViewById(R.id.etAdress)).setText(p.getValue("adress"));
		} else {
			//((EditText)this.findViewById(R.id.etAdress)).setText("");
		}
		if(p.getValue("website")!=null) {
			//((EditText)this.findViewById(R.id.etWeb)).setText(p.getValue("website"));
		} else {
			//((EditText)this.findViewById(R.id.etWeb)).setText("");
		}
		if(p.getValue("phone")!=null) {
			//((EditText)this.findViewById(R.id.etPhone)).setText(p.getValue("phone"));
		} else {
			//((EditText)this.findViewById(R.id.etPhone)).setText("");
		}
		if(p.getValue("opening_hours")!=null){
			//((EditText)this.findViewById(R.id.etHours)).setText(p.getValue("opening_hours"));
		} else {
			//((EditText)this.findViewById(R.id.etHours)).setText("");
		}
		if(p.getValue("description")!=null) {
			//((EditText)this.findViewById(R.id.etDescription)).setText(p.getValue("description"));
		} else {
			//((EditText)this.findViewById(R.id.etDescription)).setText("");
		}
		

		
	}

	protected double getLastLon() {
		return this.lastlon;
	}

	protected double getLastLat() {
		return this.lastlat;
	}

	protected void setLastLat(double lat) {
		this.lastlat=lat;
		
	}

	protected void setLastLon(double lon) {
		this.lastlon=lon;
		
	}
    protected  void clearPOIs()
    {
        MapView map=((MapView)this.findViewById(R.id.mapview));
        //Iterar per majors de 1 i eliminar-los
        while(map.getOverlays().size()>1){
            map.getOverlays().remove(1);
        }
        map.getOverlays();
        map.invalidate();
    }
	protected void addPOI(POI p)
	{
		final Context context=this;
		
		MapView map=(MapView)this.findViewById(R.id.mapview);
		Marker m=new Marker(map);

		m.setPosition(p.getGeoPoint());
		if (p.getOsmId()!=null)
		{
			m.setTitle(p.getOsmId().toString());
		}
		else
		{
			m.setTitle(String.valueOf(-1*p.getId()));
		}
		m.setDraggable(true);
		m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		if (p.getType()!=null)
		{
			if( p.getType().getIcon()==null)
			{
				
				m.setIcon(this.getResources().getDrawable(R.drawable.logo));
			}
			else
			{
				m.setIcon(p.getType().getIcon());
			}
			
		}
		m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker, MapView mapView) {
				BD bd=new BD(mapView.getContext());
				POI p=bd.getPOI(Long.valueOf(marker.getTitle()),((MainActivity)mapView.getContext()).getAvaibleTypes());
				bd.close();

                selected_marker=marker;
    			((MainActivity)mapView.getContext()).setMenuItemSelected(p,marker);
				return false;
			}
		});
		m.setOnMarkerDragListener(this);
		
		map.getOverlays().add(m);
		map.invalidate();
	}
	protected void refreshPOIs() {
		OSMAPI api=new OSMAPI(this);
		MapView map= (MapView) this.findViewById(R.id.mapview);
		if (map!=null)
		{
			BoundingBoxE6 bb = map.getBoundingBox();
			
			api.getData(filt.getActiveTypes(), (double) (bb.getLatSouthE6()/1E6), (double)(bb.getLonWestE6()/1E6),  (double)(bb.getLatNorthE6()/1E6),(double) (bb.getLonEastE6()/1E6),avaible_types,((MapView)this.findViewById(R.id.mapview)).getZoomLevel());
			
		}
		//List<POI> pois=api.getData(null,, north, south, west);
	}

	public GeoPoint getLastMapCenter() {
		return new GeoPoint(this.lastlat,this.lastlon,0);
		
	}
	public Integer getLastZoom()
	{
		return this.lastzoom;
	}
	public void setLastZoom(Integer zoom)
	{
		this.lastzoom=zoom;
	}

	protected void setMenuItemSelected() {
		menu.clear();
		getMenuInflater().inflate(R.menu.item_selected, menu);
	}
	protected void setMainMenu()
	{
		if (menu!=null)
		{
			menu.clear();
			getMenuInflater().inflate(R.menu.main, menu);
		}
	}

	@Override
	public boolean longPressHelper(GeoPoint p) {
		this.setModeAddPOI(true);
		drawerlayout.openDrawer(Gravity.START);
		this.setLatLonOfNewPOI(p.getLatitude(), p.getLongitude());
		return false;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	    if (requestCode == 1) {
	        if(resultCode == RESULT_OK){
	        	Float lat=data.getFloatExtra("_lat", 0);
	        	Float lon=data.getFloatExtra("_lon",0);
	            String name=data.getStringExtra("name");
	            String address=data.getStringExtra("address");
	            String description=data.getStringExtra("description");
	            String opening_hours=data.getStringExtra("opening_hours");
				String phone=data.getStringExtra("phone");
	            String web=data.getStringExtra("web");
	            
	            MapView map=(MapView)this.findViewById(R.id.mapview);
	            ItemizedIconOverlay<OverlayItem> layerPOIs=(ItemizedIconOverlay<OverlayItem>) map.getOverlays().get(1);
	            HashMap<String,String> tags=new HashMap<String,String>();
	            
	            if (name!=null) {
	            	tags.put("name",name );
	            }
	            if (address!=null) {
	
	            	tags.put("address",address);
	            }
	            if (description!=null) {
	            	tags.put("description",description);
	            }
	            if (opening_hours!=null) {
	            	tags.put("opening_hours",opening_hours);
	            }
	            if (phone!=null) {
	            	tags.put("phone",phone);
	            }
	            if (web!=null)
	            {
	            	tags.put("web",web);
	            }

	            BD bd=new BD(getApplicationContext());
	            Long id=bd.getFreeId();
	            POI p=new POI(lat,lon,tags, id, null,null,1);
	            p.setType(this.actualPOI_type);
	            //p.detect_type(avaible_types);
	            bd.addPOI(p, "create");
	            
	            GeoPoint gp = new GeoPoint(lat, lon,0 );
		        
	            layerPOIs.addItem(new OverlayItem(name, description, gp));
	            bd.close();
	            OSMAPI api=new OSMAPI(this);
	            List<POI> points=new ArrayList<POI>();
	            points.add(p);
	            api.addPOI(this.filt.getTypes());
	        }
	        if (resultCode == RESULT_CANCELED) {
	            //TODO Codi per descartar el nou punt
	        }
	    }
	    else if (requestCode==2)
	    {
	    	if ((data!=null)&& (data.getBooleanExtra("finish", false)==true))
            {
            	finish();
            }
	    }
	}
	class BudoLocationListener implements LocationListener {

	    @Override
	    public void onLocationChanged(Location loc) {
	        MapView map=(MapView) findViewById(R.id.mapview);
	        GeoPoint gp=new GeoPoint(loc.getLatitude(),loc.getLongitude(),0);
	        map.getController().setCenter(gp);
	    }

	    @Override
	    public void onProviderDisabled(String provider) {
	    	Toast.makeText(getApplicationContext(), getString(R.string.gps_disabled), Toast.LENGTH_LONG).show();
	    	//Intent intentGPS = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	    	//startActivity(intentGPS);
	    }

	    @Override
	    public void onProviderEnabled(String provider) {}

	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {}
		
	}
	
	public static class PlaceholderFragmentValue extends Fragment {
		MainActivity mCallback;

	    public interface OnHeadlineSelectedListener {
	        public void onArticleSelected(int position);
	    }
		public PlaceholderFragmentValue() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_values,container, false);
            MainActivity m=(MainActivity)rootView.getContext();
            //Redefinir per actual poi
            if (m.getNewPOIType()!=null) {
                List<Field> fields = m.getNewPOIType().getFields();
                Field f;
                if (fields!=null) {
                    for (int x = fields.size()-1; x >=0; x--) {
                        f = fields.get(x);
                        f.addView(this.getActivity(), (ViewGroup) rootView);
                    }
                }
                Button save = (Button) rootView.findViewById(R.id.save);
                save.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        HashMap<String, String> values = new HashMap<String, String>();
                        mCallback.setValuesOfNewPOI(values);
                        mCallback.hideValues();
                        mCallback.createNewPOI();
                    }
                });
            }
			return rootView;
		}
		@Override
		public void onAttach(Activity activity) {
		
			super.onAttach(activity);
			// This makes sure that the container activity has implemented
	        // the callback interface. If not, it throws an exception
	        try {
	            mCallback = (MainActivity) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString()+ " must implement OnHeadlineSelectedListener");
	        }
			
		}
	}
	
	public void follow() {
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,bll);
		
	}
	public void unfollow()
	{
		locationmanager.removeUpdates(bll);
	}
	public void showValues() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_values, new PlaceholderFragmentValue()).commit();
		FrameLayout frame_values = (FrameLayout) findViewById(R.id.frame_values);
		frame_values.setVisibility(View.VISIBLE);
		drawerlayout.closeDrawers();
		this.values_visible=true;
	}
	public Boolean getModeAddPOI()
	{
		return this.mode_add_poi;
	}
	public void setModeAddPOI(Boolean value)
	{
		this.mode_add_poi=value;
	}
	protected void setLatLonOfNewPOI(double lat,double lon) {
		this.newPOI_lat=lat;
		this.newPOI_lon=lon;
	}
	protected void setValuesOfNewPOI(HashMap<String, String> values) {
		this.newPOI_values=values;
	}
	protected void setNewPOIType(POIType type)
	{
		this.actualPOI_type=type;
	}
    protected  POIType getNewPOIType()
    {
        return this.actualPOI_type;
    }

	protected void createNewPOI() {

		MapView map=(MapView)this.findViewById(R.id.mapview);
		BD bd=new BD(this);
        Long id=bd.getFreeId();
        POI p=new POI(this.newPOI_lat,this.newPOI_lon,this.newPOI_values, id, null,null,1);
        p.setType(this.actualPOI_type);
        bd.addPOI(p, "created");
        bd.close();
        

        OSMAPI api=new OSMAPI(this);
        //selected_marker.setTitle(String.valueOf(1234));
        //List<POI> points=new ArrayList<POI>();
        //points.add(p);
        api.addPOI(filt.getTypes());
        this.addPOI(p);

        map.invalidate();
	}

	public void hideValues()
	{
		FrameLayout frame_values = (FrameLayout) findViewById(R.id.frame_values);
		frame_values.setVisibility(View.INVISIBLE);
		this.values_visible=false;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	        if (this.values_visible)
	        {
	        	this.hideValues();
	        	return true;
	        }
            else if (drawerlayout.isDrawerOpen(GravityCompat.START)) {
                drawerlayout.closeDrawer(Gravity.LEFT);
                return false;
            }
	    }
	    return super.onKeyDown(keyCode, event);
	}
	@Override 
	public void onStop()
	{
		super.onStop();

		this.save_state();
	}

	private void save_state() {
		MapView map=(MapView)this.findViewById(R.id.mapview);
		SharedPreferences preferences = getSharedPreferences("com.osm.mapbudo", Context.MODE_PRIVATE);
		preferences.edit().putInt("lastzoom", this.getLastZoom()).commit();
		preferences.edit().putString("lastlon",String.valueOf(map.getBoundingBox().getCenter().getLongitude()) ).commit();
		preferences.edit().putString("lastlat", String.valueOf(map.getBoundingBox().getCenter().getLatitude()) ).commit();
		this.filt.save(preferences);
	}

	public void setTypeOfNewPOI(String  groupName, Integer second) {
		this.actualPOI_type=filt.getType(groupName, second);
	}
	public void changeFilter(String groupName, Integer second, boolean checked) {
		POIType t=filt.getType(groupName, second);
		
		if (checked){
			filt.enableType(t);

		} else {
			filt.disableType(t);
            this.clearPOIs();
            this.refreshPOIs();
		}

	}
	public List<POIType> getAvaibleTypes()
	{
		return this.avaible_types;
	}

	@Override
	public void onMarkerDrag(Marker marker) {}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		MapView map=((MapView)this.findViewById(R.id.mapview));
		map.getOverlays().remove(this.marker_drag);
		map.getOverlays().add(marker);
		BD bd=new BD(this);
		POI p= bd.getPOI(Long.valueOf(marker.getTitle()), filt.getTypes());
		p.setGeoPoint(marker.getPosition());
		bd.updatePOI(p);
		bd.close();
		OSMAPI api=new OSMAPI(this);
		api.addPOI(filt.getTypes());
		map.invalidate();
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
		this.marker_drag=marker;
		
	}

	public void updateIds(List<POI> outdated_pois) {
		List<Overlay> overlay_list =((MapView)this.findViewById(R.id.mapview)).getOverlays();
		int x;
		Marker tmp;
		for (POI p:outdated_pois) {
			if( p.getStatus().equalsIgnoreCase("created")) {

				x=1;
				while (x<overlay_list.size() &&(Long.valueOf(((Marker)overlay_list.get(x)).getTitle())!=(-1*p.getId()))) {
					x++;
				}
				if ((x<overlay_list.size()) && (Long.valueOf(((Marker)overlay_list.get(x)).getTitle())==(-1*p.getId()))) {
						tmp=(Marker) overlay_list.get(x);
						tmp.setTitle(p.getOsmId().toString());
						((MapView)this.findViewById(R.id.mapview)).getOverlays().set(x, tmp);
				}
			}
		}
	}
	public Filter getFilter()
	{
		return this.filt;
	}
	public static void end() {
		
		
	}
    private void addAvaibleType(POIType t) {
        this.avaible_types.add(t);
    }
    private Filter initializeFiltter() {
        Filter filt=new Filter();
        filt.clearGroups();
        List<Field> default_fields= new ArrayList<Field>();
        default_fields.add(new Field(this,this.getResources().getString(R.string.name),"name"));
        default_fields.add(new Field(this,this.getResources().getString(R.string.adress),"adress"));
        default_fields.add(new Field(this,this.getResources().getString(R.string.web),"website"));
        default_fields.add(new Field(this,this.getResources().getString(R.string.phone),"phone"));
        default_fields.add(new Field(this,this.getResources().getString(R.string.opening_hours),"opening_hours"));
        default_fields.add(new Field(this,this.getResources().getString(R.string.description),"description"));


        POIType atm = new POIType("ATM",new HashMap<String, String>() { { put("amenity","atm"); } },getResources().getDrawable( R.drawable.atm ) );
        atm.setFields(default_fields);
        POIType bank = new POIType("Bank",new HashMap<String, String>() { { put("amenity","bank"); } },getResources().getDrawable(R.drawable.bank)) ;
        bank.setFields(default_fields);
        POIType drinking_water = new POIType("Drinking water",new HashMap<String, String>() { { put("amenity","drinking_water");}},getResources().getDrawable(R.drawable.drinkingwater) );
        drinking_water.setFields(default_fields);
        POIType hair_dresser = new POIType("Hair Dressser",new HashMap<String, String>() { { put("shop","hairdresser"); } },getResources().getDrawable(R.drawable.hairdresser));
        hair_dresser.setFields(default_fields);
        POIType laundary = new POIType("Laundry",new HashMap<String, String>() { { put("shop","laundry"); } },getResources().getDrawable(R.drawable.laundry));
        laundary.setFields(default_fields);
        POIType post_box = new POIType("Post Box",new HashMap<String, String>() { { put("amenity","post_box"); } },getResources().getDrawable(R.drawable.postbox) );
        post_box.setFields(default_fields);
        POIType post_office = new POIType("Post Office",new HashMap<String, String>() { { put("amenity","post_office"); } },getResources().getDrawable(R.drawable.postoffice));
        post_office.setFields(default_fields);
        POIType public_toilets= new POIType("Public Toilets",new HashMap<String, String>() { { put("amenity","toilets"); } },getResources().getDrawable(R.drawable.toilets));
        public_toilets.setFields(default_fields);
        POIType recycling = new POIType("Recycling",new HashMap<String, String>() { { put("amenity","recycling"); } },getResources().getDrawable(R.drawable.recycling));
        recycling.setFields(default_fields);
        POIType survillance = new POIType("Surveillance",new HashMap<String, String>() { { put("amenity","surveillance"); } },getResources().getDrawable(R.drawable.surveillance));
        survillance.setFields(default_fields);
        POIType phone = new POIType("Phone",new HashMap<String, String>() { { put("amenity","telephone"); } },getResources().getDrawable(R.drawable.telephone));
        phone.setFields(default_fields);
        filt.addGroup("Services",Arrays.asList(atm, bank, drinking_water,hair_dresser,laundary,post_box,post_office,public_toilets,recycling,survillance,phone)) ;


        POIType town_hall = new POIType("Town Hall",new HashMap<String, String>() { { put("amenity","townhall"); } },getResources().getDrawable(R.drawable.townhall));
        town_hall.setFields(default_fields);
        POIType fire_station = new POIType("Fire Station",new HashMap<String, String>() { { put("amenity","fire_station"); } },getResources().getDrawable(R.drawable.firestation));
        fire_station.setFields(default_fields);
        POIType police = new POIType("Police",new HashMap<String, String>() { { put("amenity","police"); } },getResources().getDrawable(R.drawable.police));
        police.setFields(default_fields);
        POIType prision = new POIType("Prison",new HashMap<String, String>() { { put("amenity","prison"); } },getResources().getDrawable(R.drawable.prison)) ;
        prision.setFields(default_fields);
        filt.addGroup("Goverment", Arrays.asList(town_hall,fire_station,police,prision));

        POIType auto_dealer = new POIType("Auto dealer",new HashMap<String, String>() { { put("shop","car"); } },getResources().getDrawable(R.drawable.car) );
        auto_dealer.setFields(default_fields);
        POIType car_parts = new POIType("Auto parts and accesories",new HashMap<String, String>() { { put("shop","car_parts"); } },getResources().getDrawable(R.drawable.tyres));
        car_parts.setFields(default_fields);
        POIType car_rental = new POIType("Car Rental",new HashMap<String, String>() { { put("shop","car_rental"); } },getResources().getDrawable(R.drawable.carrental));
        car_rental.setFields(default_fields);
        POIType car_repair = new POIType("Car Repair",new HashMap<String, String>() { { put("shop","car_repair"); } },getResources().getDrawable(R.drawable.repairshop)) ;
        car_repair.setFields(default_fields);
        POIType car_sharing = new POIType("Car Sharing",new HashMap<String, String>() { { put("amenity","car_sharing"); } },getResources().getDrawable(R.drawable.carsharing));
        car_sharing.setFields(default_fields);
        POIType car_wash = new POIType("Car Wash",new HashMap<String, String>() { { put("amenity","car_wash"); } },getResources().getDrawable(R.drawable.carwash));
        car_wash.setFields(default_fields);
        POIType fuel_station = new POIType("Fuel Station",new HashMap<String, String>() { { put("amenity","fuel"); } },getResources().getDrawable(R.drawable.fuel));
        fuel_station.setFields(default_fields);
        POIType parking = new POIType("Parking",new HashMap<String, String>() { { put("amenity","parking"); } },getResources().getDrawable(R.drawable.parking));
        parking.setFields(default_fields);
        filt.addGroup("Car", Arrays.asList(auto_dealer,car_rental,car_parts,car_repair,car_sharing,car_wash,fuel_station,parking));

        POIType collage = new POIType("College",new HashMap<String, String>() { { put("amenity","college"); } },getResources().getDrawable(R.drawable.college));
        collage.setFields(default_fields);
        POIType kindergarten = new POIType("Kindergarten",new HashMap<String, String>() { { put("amenity","kindergarten"); } },getResources().getDrawable(R.drawable.kindergarten));
        kindergarten.setFields(default_fields);
        POIType school = new POIType("School",new HashMap<String, String>() { { put("amenity","school"); } },getResources().getDrawable(R.drawable.school));
        school.setFields(default_fields);
        POIType university = new POIType("University",new HashMap<String, String>() { { put("amenity","university"); } },getResources().getDrawable(R.drawable.university));
        university.setFields(default_fields);
        filt.addGroup("Education", Arrays.asList(collage,kindergarten,school,university));

        POIType arts_centre = new POIType("Arts centre",new HashMap<String, String>() { { put("amenity","arts_centre"); }},getResources().getDrawable(R.drawable.artscentre));
        arts_centre.setFields(default_fields);
        POIType artwork = new POIType("Artwork",new HashMap<String, String>() { { put("tourism","artwork"); } },getResources().getDrawable(R.drawable.artscentre));
        artwork.setFields(default_fields);
        POIType cinema = new POIType("Cinema",new HashMap<String, String>() { { put("amenity","cinema"); } },getResources().getDrawable(R.drawable.cinema));
        cinema.setFields(default_fields);
        POIType library = new POIType("Library",new HashMap<String, String>() { { put("amenity","library"); }},getResources().getDrawable(R.drawable.library));
        library.setFields(default_fields);
        POIType museum = new POIType("Museum",new HashMap<String, String>() { { put("tourism","museum"); } },getResources().getDrawable(R.drawable.museum));
        museum.setFields(default_fields);
        POIType nightclub = new POIType("Nightclub",new HashMap<String, String>() { { put("amenity","nightclub"); }},getResources().getDrawable(R.drawable.nightclub));
        nightclub.setFields(default_fields);
        POIType theatre = new POIType("Theatre",new HashMap<String, String>() { { put("amenity","theatre"); }},getResources().getDrawable(R.drawable.theater));
        theatre.setFields(default_fields);
        POIType theme_park= new POIType("Theme Park",new HashMap<String, String>() { { put("amenity","theme_park"); } },getResources().getDrawable(R.drawable.themepark));
        theme_park.setFields(default_fields);
        POIType water_park= new POIType("Water park",new HashMap<String, String>() { { put("amenity","water_park"); } },getResources().getDrawable(R.drawable.waterpark));
        water_park.setFields(default_fields);
        POIType zoo = new POIType("Zoo",new HashMap<String, String>() { { put("tourism","zoo"); } },getResources().getDrawable(R.drawable.zoo));
        zoo.setFields(default_fields);
        filt.addGroup("Entretainment", Arrays.asList(arts_centre,artwork,cinema,library,museum,nightclub,theatre,theme_park,water_park,zoo));

        POIType bakery =  new POIType("Bakery",new HashMap<String, String>() { { put("shop","bakery"); } },getResources().getDrawable(R.drawable.bakery));
        bakery.setFields(default_fields);
        POIType cafe = new POIType("Cafe",new HashMap<String, String>() { { put("amenity","cafe"); } },getResources().getDrawable(R.drawable.cafe));
        cafe.setFields(default_fields);
        POIType fast_food = new POIType("Fast Food",new HashMap<String, String>() { { put("amenity","fast_food"); } },getResources().getDrawable(R.drawable.fastfood));
        fast_food.setFields(default_fields);
        POIType pub= new POIType("Pub",new HashMap<String, String>() { { put("amenity","pub"); } },getResources().getDrawable(R.drawable.pub));
        pub.setFields(default_fields);
        POIType restaurant = new POIType("Restaurant",new HashMap<String, String>() { { put("amenity","restaurant"); } },getResources().getDrawable(R.drawable.restaurant));
        restaurant.setFields(default_fields);
        POIType bar = new POIType("Bar",new HashMap<String, String>() { { put("amenity","bar"); } },getResources().getDrawable(R.drawable.bar));
        bar.setFields(default_fields);
        filt.addGroup("Food and drinks", Arrays.asList(bakery,cafe,fast_food,pub,restaurant,bar));

        POIType dentist =new POIType("Dentist",new HashMap<String, String>() { { put("helthcare","dentist"); }},getResources().getDrawable(R.drawable.dentist));
        dentist.setFields(default_fields);
        POIType doctor = new POIType("Doctor",new HashMap<String, String>() { { put("amenity","doctors"); } },getResources().getDrawable(R.drawable.doctor));
        doctor.setFields(default_fields);
        POIType hospital = new POIType("Hospital",new HashMap<String, String>() { { put("amenity","hospital"); }},getResources().getDrawable(R.drawable.hospital));
        hospital.setFields(default_fields);
        POIType pharmacy =  new POIType("Pharmacy",new HashMap<String, String>() { { put("amenity","pharmacy"); }},getResources().getDrawable(R.drawable.pharmacy));
        pharmacy.setFields(default_fields);
        POIType veterinary = new POIType("Veterinary",new HashMap<String, String>() { { put("amenity","veterinary"); }},getResources().getDrawable(R.drawable.veterinary));
        veterinary.setFields(default_fields);
        filt.addGroup("Health care & Vet", Arrays.asList(dentist,doctor,hospital,pharmacy,veterinary));

        POIType adult = new POIType("Adult",new HashMap<String, String>() { { put("shop","erotic"); } },getResources().getDrawable(R.drawable.erotic));
        adult.setFields(default_fields);
        POIType alcohol = new POIType("Alcohol",new HashMap<String, String>() { { put("shop","alcohol"); } },getResources().getDrawable(R.drawable.alcohol));
        alcohol.setFields(default_fields);
        POIType bicycle = new POIType("Bicycle",new HashMap<String, String>() { { put("shop","bicycle"); } },getResources().getDrawable(R.drawable.bicycle));
        bicycle.setFields(default_fields);
        POIType books =  new POIType("Books",new HashMap<String, String>() { { put("shop","books"); } },getResources().getDrawable(R.drawable.book));
        books.setFields(default_fields);
        POIType butcher= new POIType("Butcher",new HashMap<String, String>() { { put("shop","butcher"); } },getResources().getDrawable(R.drawable.butcher));
        butcher.setFields(default_fields);
        POIType clothes = new POIType("Clothes",new HashMap<String, String>() { { put("shop","clothes"); } },getResources().getDrawable(R.drawable.clothes));
        clothes.setFields(default_fields);
        POIType computers = new POIType("Computers",new HashMap<String, String>() { { put("shop","computer"); } },getResources().getDrawable(R.drawable.computer));
        computers.setFields(default_fields);
        POIType convinience = new POIType("Convinience",new HashMap<String, String>() { { put("shop","convenience"); } },getResources().getDrawable(R.drawable.convenience));
        convinience.setFields(default_fields);
        POIType electronics = new POIType("Electronics",new HashMap<String, String>() { { put("shop","electronics"); } },getResources().getDrawable(R.drawable.electronics));
        electronics.setFields(default_fields);
        POIType florist = new POIType("Florist",new HashMap<String, String>() { { put("shop","florist"); } },getResources().getDrawable(R.drawable.florist));
        florist.setFields(default_fields);
        POIType furniture = new POIType("Furniture",new HashMap<String, String>() { { put("shop","furniture"); } },getResources().getDrawable(R.drawable.furniture));
        furniture.setFields(default_fields);
        POIType sports = new POIType("Sports",new HashMap<String, String>() { { put("shop","sports"); } },getResources().getDrawable(R.drawable.sports));
        sports.setFields(default_fields);
        POIType jewelers =new POIType("Jewelers",new HashMap<String, String>() { { put("shop","jewelry"); } },getResources().getDrawable(R.drawable.jewelry));
        jewelers.setFields(default_fields);
        POIType mobile = new POIType("Mobile phones",new HashMap<String, String>() { { put("shop","mobile_phone"); } },getResources().getDrawable(R.drawable.mobilephone));
        mobile.setFields(default_fields);
        POIType music = new POIType("Music",new HashMap<String, String>() { { put("shop","music"); } },getResources().getDrawable(R.drawable.music));
        music.setFields(default_fields);
        POIType outdoor= new POIType("Outdoor",new HashMap<String, String>() { { put("shop","outdoor"); } },getResources().getDrawable(R.drawable.outdoor));
        outdoor.setFields(default_fields);
        POIType photo = new POIType("Photo",new HashMap<String, String>() { { put("shop","photo"); } },getResources().getDrawable(R.drawable.photo));
        photo.setFields(default_fields);
        POIType shoes= new POIType("Shoes",new HashMap<String, String>() { { put("shop","shoes"); } },getResources().getDrawable(R.drawable.shoes));
        shoes.setFields(default_fields);
        POIType souvenirs =  new POIType("Sovenirs",new HashMap<String, String>() { { put("shop","gift"); } },getResources().getDrawable(R.drawable.gift));
        souvenirs.setFields(default_fields);
        POIType stationery=new POIType("Stationery",new HashMap<String, String>() { { put("shop","stationery"); } },getResources().getDrawable(R.drawable.stationery));
        stationery.setFields(default_fields);
        POIType supermarket= new POIType("Supermarket",new HashMap<String, String>() { { put("shop","supermarket"); } },getResources().getDrawable(R.drawable.supermarket));
        supermarket.setFields(default_fields);
        POIType toys= new POIType("Toys",new HashMap<String, String>() { { put("shop","toys"); } },getResources().getDrawable(R.drawable.toys));
        toys.setFields(default_fields);
        filt.addGroup("Shoping", Arrays.asList(adult,alcohol,bicycle,books,butcher,clothes,computers,convinience,electronics,florist,furniture,sports,jewelers,mobile,music,outdoor,photo,shoes,souvenirs,stationery,supermarket,toys));


        POIType archery=new POIType("Archery",new HashMap<String, String>() { { put("sport","archery"); } },getResources().getDrawable(R.drawable.archery));
        archery.setFields(default_fields);
        POIType athletics=new POIType("Athletics",new HashMap<String, String>() { { put("sport","athletics"); } },getResources().getDrawable(R.drawable.athletics));
        athletics.setFields(default_fields);
        POIType baseball = new POIType("Baseball",new HashMap<String, String>() { { put("sport","baseball"); } },getResources().getDrawable(R.drawable.baseball));
        baseball.setFields(default_fields);
        POIType basketball= new POIType("Basketball",new HashMap<String, String>() { { put("sport","basketball"); }},getResources().getDrawable(R.drawable.basketball));
        basketball.setFields(default_fields);
        POIType boules= new POIType("Boules",new HashMap<String, String>() { { put("sport","boules"); } },getResources().getDrawable(R.drawable.boule));
        boules.setFields(default_fields);
        POIType bowls= new POIType("Bowls",new HashMap<String, String>() { { put("sport","bowls"); } },getResources().getDrawable(R.drawable.bowls));
        bowls.setFields(default_fields);
        POIType canoe = new POIType("Caone",new HashMap<String, String>() { { put("sport","canoe"); } },getResources().getDrawable(R.drawable.canoe));
        canoe.setFields(default_fields);
        POIType climbing = new POIType("Climbing",new HashMap<String, String>() { { put("sport","climbing"); } },getResources().getDrawable(R.drawable.hillclimbing));
        climbing.setFields(default_fields);
        POIType equestrian = new POIType("Equestrian",new HashMap<String, String>() { { put("sport","equestrian"); } },getResources().getDrawable(R.drawable.equestrian));
        equestrian.setFields(default_fields);
        POIType motor_sport = new POIType("Motor sport",new HashMap<String, String>() { { put("sport","motor"); } },getResources().getDrawable(R.drawable.motorbike));
        motor_sport.setFields(default_fields);
        POIType multi= new POIType("Multi",new HashMap<String, String>() { { put("sport","multi"); } },getResources().getDrawable(R.drawable.multi));
        multi.setFields(default_fields);
        POIType pitch = new POIType("Pitch",new HashMap<String, String>() { { put("leisure","pitch"); } },getResources().getDrawable(R.drawable.pitch));
        pitch.setFields(default_fields);
        POIType rugby= new POIType("Rugby",new HashMap<String, String>() { { put("sport","rugby_union"); } },getResources().getDrawable(R.drawable.football));
        rugby.setFields(default_fields);
        POIType shooting= new POIType("Shooting",new HashMap<String, String>() { { put("sport","shooting"); } },getResources().getDrawable(R.drawable.shooting));
        shooting.setFields(default_fields);
        POIType skateboard= new POIType("Skateboard",new HashMap<String, String>() { { put("sport","skateboard"); } },getResources().getDrawable(R.drawable.skateboard));
        skateboard.setFields(default_fields);
        POIType skating=new POIType("Skating",new HashMap<String, String>() { { put("sport","skating"); } },getResources().getDrawable(R.drawable.skating));
        skating.setFields(default_fields);
        POIType soccer= new POIType("Soccer",new HashMap<String, String>() { { put("sport","soccer"); } },getResources().getDrawable(R.drawable.soccer));
        soccer.setFields(default_fields);
        POIType sport_center= new POIType("Sport Center",new HashMap<String, String>() { { put("leisure","sports_centre"); } },getResources().getDrawable(R.drawable.sportscentre));
        sport_center.setFields(default_fields);
        POIType stadium=new POIType("Stadium",new HashMap<String, String>() { { put("leisure","stadium"); } },getResources().getDrawable(R.drawable.stadium));
        stadium.setFields(default_fields);
        POIType swing= new POIType("Stadium",new HashMap<String, String>() { { put("leisure","stadium"); } },getResources().getDrawable(R.drawable.stadium));
        swing.setFields(default_fields);
        POIType table_tenis= new POIType("Table Tenis",new HashMap<String, String>() { { put("sport","table_tenis"); } },getResources().getDrawable(R.drawable.tabletennis));
        table_tenis.setFields(default_fields);
        POIType tenis = new POIType("Tenis",new HashMap<String, String>() { { put("sport","tenis"); } },getResources().getDrawable(R.drawable.tennis));
        tenis.setFields(default_fields);
        POIType track =new POIType("Track",new HashMap<String, String>() { { put("leisure","track"); } },getResources().getDrawable(R.drawable.track));
        track.setFields(default_fields);
        filt.addGroup("Sport",Arrays.asList(archery,athletics,baseball,basketball,boules,bowls,canoe,climbing,equestrian,motor_sport,multi,pitch,shooting,skateboard,skating,soccer,stadium,sport_center,swing,table_tenis));

        POIType bicycle_parking=new POIType("Bicycle parking",new HashMap<String, String>() { { put("amenity","bicycle_parking"); } },getResources().getDrawable(R.drawable.bikeparking));
        bicycle_parking.setFields(default_fields);
        POIType bicycle_rental=new POIType("Bicycle Rental",new HashMap<String, String>() { { put("amenity","bicycle_rental"); } },getResources().getDrawable(R.drawable.bicyclerental));
        bicycle_rental.setFields(default_fields);
        POIType bus_stop=new POIType("Bus stop",new HashMap<String, String>() { { put("highway","bus_stop"); } },getResources().getDrawable(R.drawable.bus));
        bus_stop.setFields(default_fields);
        POIType taxi_stop = new POIType("Taxi stop",new HashMap<String, String>() { { put("amenity","taxi"); } },getResources().getDrawable(R.drawable.taxi));
        taxi_stop.setFields(default_fields);
        POIType train_stop= new POIType("Train station",new HashMap<String, String>() { { put("building","train_station"); } },getResources().getDrawable(R.drawable.train));
        train_stop.setFields(default_fields);
        POIType tram_stop= new POIType("Tram stop",new HashMap<String, String>() { { put("railway","tram_stop"); } },getResources().getDrawable(R.drawable.tram));
        tram_stop.setFields(default_fields);
        filt.addGroup("Transport", Arrays.asList(bicycle_parking,bicycle_rental,bus_stop,taxi_stop,train_stop,tram_stop));

        //Add avaible types

        this.avaible_types=new ArrayList<POIType>();
        this.addAvaibleType(atm);
        this.addAvaibleType(bank);
        this.addAvaibleType(drinking_water);
        this.addAvaibleType(hair_dresser);
        this.addAvaibleType(laundary);
        this.addAvaibleType(post_box);
        this.addAvaibleType(post_office);
        this.addAvaibleType(public_toilets);
        this.addAvaibleType(recycling);
        this.addAvaibleType(survillance);
        this.addAvaibleType(phone);

        this.addAvaibleType(town_hall);
        this.addAvaibleType(fire_station);
        this.addAvaibleType(police);
        this.addAvaibleType(prision);

        this.addAvaibleType(auto_dealer);
        this.addAvaibleType(car_parts);
        this.addAvaibleType(car_rental);
        this.addAvaibleType(car_repair);
        this.addAvaibleType(car_sharing);
        this.addAvaibleType(car_wash);
        this.addAvaibleType(fuel_station);
        this.addAvaibleType(parking);

        this.addAvaibleType(collage);
        this.addAvaibleType(kindergarten);
        this.addAvaibleType(school);
        this.addAvaibleType(university);

        this.addAvaibleType(arts_centre);
        this.addAvaibleType(artwork);
        this.addAvaibleType(cinema);
        this.addAvaibleType(library);
        this.addAvaibleType(museum);
        this.addAvaibleType(nightclub);
        this.addAvaibleType(theatre);
        this.addAvaibleType(theme_park);
        this.addAvaibleType(water_park);
        this.addAvaibleType(zoo);

        this.addAvaibleType(bakery);
        this.addAvaibleType(cafe);
        this.addAvaibleType(fast_food);
        this.addAvaibleType(pub);
        this.addAvaibleType(restaurant);
        this.addAvaibleType(bar);

        this.addAvaibleType(dentist);
        this.addAvaibleType(doctor);
        this.addAvaibleType(hospital);
        this.addAvaibleType(pharmacy);
        this.addAvaibleType(veterinary);

        this.addAvaibleType(adult);
        this.addAvaibleType(alcohol);
        this.addAvaibleType(bicycle);
        this.addAvaibleType(books);
        this.addAvaibleType(butcher);
        this.addAvaibleType(clothes);
        this.addAvaibleType(computers);
        this.addAvaibleType(convinience);
        this.addAvaibleType(electronics);
        this.addAvaibleType(florist);
        this.addAvaibleType(furniture);
        this.addAvaibleType(sports);
        this.addAvaibleType(jewelers);
        this.addAvaibleType(mobile);
        this.addAvaibleType(music);
        this.addAvaibleType(outdoor);
        this.addAvaibleType(photo);
        this.addAvaibleType(shoes);
        this.addAvaibleType(souvenirs);
        this.addAvaibleType(stationery);
        this.addAvaibleType(toys);

        this.addAvaibleType(archery);
        this.addAvaibleType(athletics);
        this.addAvaibleType(baseball);
        this.addAvaibleType(boules);
        this.addAvaibleType(canoe);
        this.addAvaibleType(climbing);
        this.addAvaibleType(equestrian);
        this.addAvaibleType(motor_sport);
        this.addAvaibleType(multi);
        this.addAvaibleType(pitch);
        this.addAvaibleType(rugby);
        this.addAvaibleType(shooting);
        this.addAvaibleType(skateboard);
        this.addAvaibleType(soccer);
        this.addAvaibleType(sport_center);
        this.addAvaibleType(swing);
        this.addAvaibleType(table_tenis);
        this.addAvaibleType(tenis);
        this.addAvaibleType(track);

        this.addAvaibleType(bicycle_parking);
        this.addAvaibleType(bicycle_rental);
        this.addAvaibleType(bus_stop);
        this.addAvaibleType(taxi_stop);
        this.addAvaibleType(train_stop);
        this.addAvaibleType(tram_stop);

        return filt;
    }
}

