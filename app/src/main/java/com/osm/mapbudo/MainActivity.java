package com.osm.mapbudo;

//TODO He trobat molt a faltar un «marker» quan vas a crear un node. Encara que sigui una cosa senzilla[1], més que res per saber on afegiràs informació.
//TODO El menú de l'esquerra, amb les etiquetes mostra molta informació. Hi ha moltes etiquetes i això mola. També és veritat, però, que en falten moltes... I jo pensava...
//TODO No molaria tenir un espai on poder dir quines categories vols que apareguin al menú i quines no? Per exemple, potser jo sé que no entraré info de transports, llavors amago aquella categoria
//TODO O encara més... Molaria molt que poguessis escollir perfils. Per exemple, perfil «Muntanya» amb etiquetes del tipus de nodes que podries trobar allà, o perfil «Ciutat» o altres...
//TODO Etiquetes com «Opening_hours» haurien de disposar d'algun tipus d'ajuda. Un desplegable amb exemples o quelcom similar. Jo no tinc pebrots d'emplenar aquesta etiqueta sense una xuleta.
//TODO Per etiquetes amb molts valors (com «Services» o «Shoping») estaria molt bé plegar la categoria des del darrer valor de la categoria, ja que si no t'obliga a fer scroll fins a dalt de tot per poder tancar-la.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.BroadcastReceiver;

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

import android.app.Dialog;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.DialogInterface;

public class MainActivity extends ActionBarActivity implements MapEventsReceiver,OnMarkerDragListener {

	//private ItemizedIconOverlay<OverlayItem> layerPOIs;
	private DrawerLayout drawerlayout;
	private ActionBarDrawerToggle drawertoggle;
	private ExpandableListView drawerlist;
	private boolean values_visible;
	private HashMap<String, String> newPOI_values;
	private double newPOI_lon;
	private double newPOI_lat;
	private POIType newPOI_type;
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
		menu=null;
		locationmanager = (LocationManager)this.getApplicationContext().getSystemService(LOCATION_SERVICE);
		bll=new BudoLocationListener();
		drawerlayout=(DrawerLayout) this.findViewById(R.id.drawer_layout);
		drawerlist=(ExpandableListView) this.findViewById(R.id.left_drawer);
		
		
		filt=new Filter();
		filt.clearGroups();
		filt.addGroup("Services", new ArrayList<POIType>(){{
			add(new POIType("ATM",new HashMap<String, String>() { { put("amenity","atm"); } },getResources().getDrawable( R.drawable.atm ) ));
			add(new POIType("Bank",new HashMap<String, String>() { { put("amenity","bank"); } },getResources().getDrawable(R.drawable.bank)) );
			add(new POIType("Drinking water",new HashMap<String, String>() { { put("amenity","drinking_water");}},getResources().getDrawable(R.drawable.drinkingwater) ) );
			add(new POIType("Hair Dressser",new HashMap<String, String>() { { put("shop","hairdresser"); } },getResources().getDrawable(R.drawable.hairdresser)) );//
			add(new POIType("Laundry",new HashMap<String, String>() { { put("shop","laundry"); } },getResources().getDrawable(R.drawable.laundry)) );//
			add(new POIType("Post Box",new HashMap<String, String>() { { put("amenity","post_box"); } },getResources().getDrawable(R.drawable.postbox)) );//
			add(new POIType("Post Office",new HashMap<String, String>() { { put("amenity","post_office"); } },getResources().getDrawable(R.drawable.postoffice)) );//		
			add(new POIType("Public Toilets",new HashMap<String, String>() { { put("amenity","toilets"); } },getResources().getDrawable(R.drawable.toilets)) );//
			add(new POIType("Recycling",new HashMap<String, String>() { { put("amenity","recycling"); } },getResources().getDrawable(R.drawable.recycling)) );//
			add(new POIType("Surveillance",new HashMap<String, String>() { { put("amenity","surveillance"); } },getResources().getDrawable(R.drawable.surveillance)) );
			add(new POIType("Phone",new HashMap<String, String>() { { put("amenity","telephone"); } },getResources().getDrawable(R.drawable.telephone)) );//
			
		}});
		filt.addGroup("Goverment", new ArrayList<POIType>(){{
			add(new POIType("Town Hall",new HashMap<String, String>() { { put("amenity","townhall"); } },getResources().getDrawable(R.drawable.townhall)) );
			add(new POIType("Fire Station",new HashMap<String, String>() { { put("amenity","fire_station"); } },getResources().getDrawable(R.drawable.firestation)) );
			add(new POIType("Police",new HashMap<String, String>() { { put("amenity","police"); } },getResources().getDrawable(R.drawable.police)) );
			add(new POIType("Prison",new HashMap<String, String>() { { put("amenity","prison"); } },getResources().getDrawable(R.drawable.prison)) );
		}});
		filt.addGroup("Car", new ArrayList<POIType>(){{
			add(new POIType("Auto dealer",new HashMap<String, String>() { { put("shop","car"); } },getResources().getDrawable(R.drawable.car) ));
			add(new POIType("Auto parts and accesories",new HashMap<String, String>() { { put("shop","car_parts"); } },getResources().getDrawable(R.drawable.tyres)) );
			add(new POIType("Car Rental",new HashMap<String, String>() { { put("shop","car_rental"); } },getResources().getDrawable(R.drawable.carrental)) );
			add(new POIType("Car Repair",new HashMap<String, String>() { { put("shop","car_repair"); } },getResources().getDrawable(R.drawable.repairshop)) );
			add(new POIType("Car Sharing",new HashMap<String, String>() { { put("amenity","car_sharing"); } },getResources().getDrawable(R.drawable.carsharing)) );
			add(new POIType("Car Wash",new HashMap<String, String>() { { put("amenity","car_wash"); } },getResources().getDrawable(R.drawable.carwash)) );
			add(new POIType("Fuel Station",new HashMap<String, String>() { { put("amenity","fuel"); } },getResources().getDrawable(R.drawable.fuel)) );
			add(new POIType("Parking",new HashMap<String, String>() { { put("amenity","parking"); } },getResources().getDrawable(R.drawable.parking)) );			
		}});
		filt.addGroup("Education", new ArrayList<POIType>(){{
			add(new POIType("College",new HashMap<String, String>() { { put("amenity","college"); } },getResources().getDrawable(R.drawable.college)) );
			add(new POIType("Kindergarten",new HashMap<String, String>() { { put("amenity","kindergarten"); } },getResources().getDrawable(R.drawable.kindergarten)) );
			add(new POIType("School",new HashMap<String, String>() { { put("amenity","school"); } },getResources().getDrawable(R.drawable.school)) );
			add(new POIType("University",new HashMap<String, String>() { { put("amenity","university"); } },getResources().getDrawable(R.drawable.university)) );
		}});
		filt.addGroup("Entretainment", new ArrayList<POIType>(){{
			add(new POIType("Arts centre",new HashMap<String, String>() { { put("amenity","arts_centre"); }},getResources().getDrawable(R.drawable.artscentre)) );
			add(new POIType("Artwork",new HashMap<String, String>() { { put("tourism","artwork"); } },getResources().getDrawable(R.drawable.artscentre) ) );
			add(new POIType("Cinema",new HashMap<String, String>() { { put("amenity","cinema"); } },getResources().getDrawable(R.drawable.cinema)) );
			add(new POIType("Library",new HashMap<String, String>() { { put("amenity","library"); }},getResources().getDrawable(R.drawable.library) ) );
			add(new POIType("Museum",new HashMap<String, String>() { { put("tourism","museum"); } },getResources().getDrawable(R.drawable.museum)) );
			add(new POIType("Nightclub",new HashMap<String, String>() { { put("amenity","nightclub"); }},getResources().getDrawable(R.drawable.nightclub)) );
			add(new POIType("Theatre",new HashMap<String, String>() { { put("amenity","theatre"); }},getResources().getDrawable(R.drawable.theater)) );
			add(new POIType("Theme Park",new HashMap<String, String>() { { put("amenity","theme_park"); } },getResources().getDrawable(R.drawable.themepark)) );
			add(new POIType("Water park",new HashMap<String, String>() { { put("amenity","water_park"); } },getResources().getDrawable(R.drawable.waterpark)) );
			add(new POIType("Zoo",new HashMap<String, String>() { { put("tourism","zoo"); } },getResources().getDrawable(R.drawable.zoo)) );
		}});
		filt.addGroup("Food and drinks", new ArrayList<POIType>(){{
			add(new POIType("Bakery",new HashMap<String, String>() { { put("shop","bakery"); } },getResources().getDrawable(R.drawable.bakery)) );//
			add(new POIType("Cafe",new HashMap<String, String>() { { put("amenity","cafe"); } },getResources().getDrawable(R.drawable.cafe)) );//
			add(new POIType("Fast Food",new HashMap<String, String>() { { put("amenity","fast_food"); } },getResources().getDrawable(R.drawable.fastfood)) );//
			add(new POIType("Pub",new HashMap<String, String>() { { put("amenity","pub"); } },getResources().getDrawable(R.drawable.pub)) );//
			add(new POIType("Restaurant",new HashMap<String, String>() { { put("amenity","restaurant"); } },getResources().getDrawable(R.drawable.restaurant)) );//
			add(new POIType("Bar",new HashMap<String, String>() { { put("amenity","bar"); } },getResources().getDrawable(R.drawable.bar)) );//
			
		}});
		filt.addGroup("Health care & Vet", new ArrayList<POIType>(){{
			add(new POIType("Dentist",new HashMap<String, String>() { { put("helthcare","dentist"); }},getResources().getDrawable(R.drawable.dentist) ) );//
			add(new POIType("Doctor",new HashMap<String, String>() { { put("amenity","doctors"); } },getResources().getDrawable(R.drawable.doctor)) );//
			add(new POIType("Hospital",new HashMap<String, String>() { { put("amenity","hospital"); }},getResources().getDrawable(R.drawable.hospital)) );//
			add(new POIType("Pharmacy",new HashMap<String, String>() { { put("amenity","pharmacy"); }},getResources().getDrawable(R.drawable.pharmacy)) );//
			add(new POIType("Veterinary",new HashMap<String, String>() { { put("amenity","veterinary"); }},getResources().getDrawable(R.drawable.veterinary) ) ); //
		}});
		filt.addGroup("Shoping", new ArrayList<POIType>(){{
			add(new POIType("Adult",new HashMap<String, String>() { { put("shop","erotic"); } },getResources().getDrawable(R.drawable.erotic)) );
			add(new POIType("Alcohol",new HashMap<String, String>() { { put("shop","alcohol"); } },getResources().getDrawable(R.drawable.alcohol)) );
			add(new POIType("Bicycle",new HashMap<String, String>() { { put("shop","bicycle"); } },getResources().getDrawable(R.drawable.bicycle)) );
			add(new POIType("Books",new HashMap<String, String>() { { put("shop","books"); } },getResources().getDrawable(R.drawable.book) ));
			add(new POIType("Butcher",new HashMap<String, String>() { { put("shop","butcher"); } },getResources().getDrawable(R.drawable.butcher)) );
			add(new POIType("Clothes",new HashMap<String, String>() { { put("shop","clothes"); } },getResources().getDrawable(R.drawable.clothes)) );
			add(new POIType("Computers",new HashMap<String, String>() { { put("shop","computer"); } },getResources().getDrawable(R.drawable.computer)) );
			add(new POIType("Convinience",new HashMap<String, String>() { { put("shop","convenience"); } },getResources().getDrawable(R.drawable.convenience)) );
			add(new POIType("Electronics",new HashMap<String, String>() { { put("shop","electronics"); } },getResources().getDrawable(R.drawable.electronics)) );
			add(new POIType("Florist",new HashMap<String, String>() { { put("shop","florist"); } },getResources().getDrawable(R.drawable.florist)) );
			add(new POIType("Furniture",new HashMap<String, String>() { { put("shop","furniture"); } },getResources().getDrawable(R.drawable.furniture)) );
			add(new POIType("Sports",new HashMap<String, String>() { { put("shop","sports"); } },getResources().getDrawable(R.drawable.sports)) );
			add(new POIType("Jewelers",new HashMap<String, String>() { { put("shop","jewelry"); } },getResources().getDrawable(R.drawable.jewelry)) );
			add(new POIType("Mobile phones",new HashMap<String, String>() { { put("shop","mobile_phone"); } },getResources().getDrawable(R.drawable.mobilephone)) );
			add(new POIType("Music",new HashMap<String, String>() { { put("shop","music"); } },getResources().getDrawable(R.drawable.music)) );
			add(new POIType("Outdoor",new HashMap<String, String>() { { put("shop","outdoor"); } },getResources().getDrawable(R.drawable.outdoor)) );
			add(new POIType("Photo",new HashMap<String, String>() { { put("shop","photo"); } },getResources().getDrawable(R.drawable.photo)) );
			add(new POIType("Shoes",new HashMap<String, String>() { { put("shop","shoes"); } },getResources().getDrawable(R.drawable.shoes)) );
			add(new POIType("Sovenirs",new HashMap<String, String>() { { put("shop","gift"); } },getResources().getDrawable(R.drawable.gift)) );
			add(new POIType("Stationery",new HashMap<String, String>() { { put("shop","stationery"); } },getResources().getDrawable(R.drawable.stationery)) );
			add(new POIType("Supermarket",new HashMap<String, String>() { { put("shop","supermarket"); } },getResources().getDrawable(R.drawable.supermarket)) );
			add(new POIType("Toys",new HashMap<String, String>() { { put("shop","toys"); } },getResources().getDrawable(R.drawable.toys)) );
		}});
		filt.addGroup("Sport", new ArrayList<POIType>(){{
			add(new POIType("Archery",new HashMap<String, String>() { { put("sport","archery"); } },getResources().getDrawable(R.drawable.archery)) );
			add(new POIType("Athletics",new HashMap<String, String>() { { put("sport","athletics"); } },getResources().getDrawable(R.drawable.athletics)) );
			add(new POIType("Baseball",new HashMap<String, String>() { { put("sport","baseball"); } },getResources().getDrawable(R.drawable.baseball)) );
			add(new POIType("Basketball",new HashMap<String, String>() { { put("sport","basketball"); }},getResources().getDrawable(R.drawable.basketball)) );
			add(new POIType("Boules",new HashMap<String, String>() { { put("sport","boules"); } },getResources().getDrawable(R.drawable.boule)) );
			add(new POIType("Bowls",new HashMap<String, String>() { { put("sport","bowls"); } },getResources().getDrawable(R.drawable.bowls)) );
			add(new POIType("Caone",new HashMap<String, String>() { { put("sport","canoe"); } },getResources().getDrawable(R.drawable.canoe)) );
			add(new POIType("Climbing",new HashMap<String, String>() { { put("sport","climbing"); } },getResources().getDrawable(R.drawable.hillclimbing)) );
			add(new POIType("Equestrian",new HashMap<String, String>() { { put("sport","equestrian"); } },getResources().getDrawable(R.drawable.equestrian)) );
			add(new POIType("Motor sport",new HashMap<String, String>() { { put("sport","motor"); } },getResources().getDrawable(R.drawable.motorbike)) );
			add(new POIType("Multi",new HashMap<String, String>() { { put("sport","multi"); } },getResources().getDrawable(R.drawable.multi)) );
			add(new POIType("Pitch",new HashMap<String, String>() { { put("leisure","pitch"); } },getResources().getDrawable(R.drawable.pitch)) );
			add(new POIType("Rugby",new HashMap<String, String>() { { put("sport","rugby_union"); } },getResources().getDrawable(R.drawable.football)) );
			add(new POIType("Shooting",new HashMap<String, String>() { { put("sport","shooting"); } },getResources().getDrawable(R.drawable.shooting)) );
			add(new POIType("Skateboard",new HashMap<String, String>() { { put("sport","skateboard"); } },getResources().getDrawable(R.drawable.skateboard)) );
			add(new POIType("Skating",new HashMap<String, String>() { { put("sport","skating"); } },getResources().getDrawable(R.drawable.skating)) );
			add(new POIType("Soccer",new HashMap<String, String>() { { put("sport","soccer"); } },getResources().getDrawable(R.drawable.soccer)) );
			add(new POIType("Sport Center",new HashMap<String, String>() { { put("leisure","sports_centre"); } },getResources().getDrawable(R.drawable.sportscentre)) );
			add(new POIType("Stadium",new HashMap<String, String>() { { put("leisure","stadium"); } },getResources().getDrawable(R.drawable.stadium)) );
			add(new POIType("Swiming",new HashMap<String, String>() { { put("sport","swiming"); } },getResources().getDrawable(R.drawable.swimming)) );
			add(new POIType("Table Tenis",new HashMap<String, String>() { { put("sport","table_tenis"); } },getResources().getDrawable(R.drawable.tabletennis)) );
			add(new POIType("Tenis",new HashMap<String, String>() { { put("sport","tenis"); } },getResources().getDrawable(R.drawable.tennis)) );
			add(new POIType("Track",new HashMap<String, String>() { { put("leisure","track"); } },getResources().getDrawable(R.drawable.track)) );
		}});
		filt.addGroup("Transport", new ArrayList<POIType>(){{
			add(new POIType("Bicycle parking",new HashMap<String, String>() { { put("amenity","bicycle_parking"); } },getResources().getDrawable(R.drawable.bikeparking)) );//
			add(new POIType("Bicycle Rental",new HashMap<String, String>() { { put("amenity","bicycle_rental"); } },getResources().getDrawable(R.drawable.bicyclerental)) );//
			add(new POIType("Bus stop",new HashMap<String, String>() { { put("highway","bus_stop"); } },getResources().getDrawable(R.drawable.bus)) );//
			add(new POIType("Taxi stop",new HashMap<String, String>() { { put("amenity","taxi"); } },getResources().getDrawable(R.drawable.taxi)) );//
			add(new POIType("Train station",new HashMap<String, String>() { { put("building","train_station"); } },getResources().getDrawable(R.drawable.train)) );//
			add(new POIType("Tram stop",new HashMap<String, String>() { { put("railway","tram_stop"); } },getResources().getDrawable(R.drawable.tram)) );//
		}});
		//Add avaible types
		
		avaible_types=new ArrayList<POIType>();
		avaible_types.add(new POIType("ATM",new HashMap<String, String>() { { put("amenity","atm"); } },getResources().getDrawable( R.drawable.atm ) ));
		avaible_types.add(new POIType("Bank",new HashMap<String, String>() { { put("amenity","bank"); } },getResources().getDrawable(R.drawable.bank)) );
		avaible_types.add(new POIType("Drinking water",new HashMap<String, String>() { { put("amenity","drinking_water");}},getResources().getDrawable(R.drawable.drinkingwater) ) );//
		avaible_types.add(new POIType("Hair Dressser",new HashMap<String, String>() { { put("shop","hairdresser"); } },getResources().getDrawable(R.drawable.hairdresser)) );//
		avaible_types.add(new POIType("Laundry",new HashMap<String, String>() { { put("shop","laundry"); } },getResources().getDrawable(R.drawable.laundry)) );//
		avaible_types.add(new POIType("Post Box",new HashMap<String, String>() { { put("amenity","post_box"); } },getResources().getDrawable(R.drawable.postbox)) );//
		avaible_types.add(new POIType("Post Office",new HashMap<String, String>() { { put("amenity","post_office"); } },getResources().getDrawable(R.drawable.postoffice)) );//		
		avaible_types.add(new POIType("Public Toilets",new HashMap<String, String>() { { put("amenity","toilets"); } },getResources().getDrawable(R.drawable.toilets)) );//
		avaible_types.add(new POIType("Recycling",new HashMap<String, String>() { { put("amenity","recycling"); } },getResources().getDrawable(R.drawable.recycling)) );//
		avaible_types.add(new POIType("Surveillance",new HashMap<String, String>() { { put("amenity","surveillance"); } },getResources().getDrawable(R.drawable.surveillance)) );
		avaible_types.add(new POIType("Phone",new HashMap<String, String>() { { put("amenity","telephone"); } },getResources().getDrawable(R.drawable.telephone)) );//
		
		avaible_types.add(new POIType("Town Hall",new HashMap<String, String>() { { put("amenity","townhall"); } },getResources().getDrawable(R.drawable.townhall)) );//
		avaible_types.add(new POIType("Fire Station",new HashMap<String, String>() { { put("amenity","fire_station"); } },getResources().getDrawable(R.drawable.firestation)) );//
		avaible_types.add(new POIType("Police",new HashMap<String, String>() { { put("amenity","police"); } },getResources().getDrawable(R.drawable.police)) );//
		avaible_types.add(new POIType("Prison",new HashMap<String, String>() { { put("amenity","prison"); } },getResources().getDrawable(R.drawable.prison)) );//
		
		
		avaible_types.add(new POIType("Auto dealer",new HashMap<String, String>() { { put("shop","car"); } },getResources().getDrawable(R.drawable.car) ));//
		avaible_types.add(new POIType("Auto parts and accesories",new HashMap<String, String>() { { put("shop","car_parts"); } },getResources().getDrawable(R.drawable.tyres)) );//
		avaible_types.add(new POIType("Car Rental",new HashMap<String, String>() { { put("shop","car_rental"); } },getResources().getDrawable(R.drawable.carrental)) );//
		avaible_types.add(new POIType("Car Repair",new HashMap<String, String>() { { put("shop","car_repair"); } },getResources().getDrawable(R.drawable.repairshop)) );//
		avaible_types.add(new POIType("Car Sharing",new HashMap<String, String>() { { put("amenity","car_sharing"); } },getResources().getDrawable(R.drawable.carsharing)) );//
		avaible_types.add(new POIType("Car Wash",new HashMap<String, String>() { { put("amenity","car_wash"); } },getResources().getDrawable(R.drawable.carwash)) );//
		avaible_types.add(new POIType("Fuel Station",new HashMap<String, String>() { { put("amenity","fuel"); } },getResources().getDrawable(R.drawable.fuel)) );//
		avaible_types.add(new POIType("Parking",new HashMap<String, String>() { { put("amenity","parking"); } },getResources().getDrawable(R.drawable.parking)) );//
		
		avaible_types.add(new POIType("College",new HashMap<String, String>() { { put("amenity","college"); } },getResources().getDrawable(R.drawable.college)) );
		avaible_types.add(new POIType("Kindergarten",new HashMap<String, String>() { { put("amenity","kindergarten"); } },getResources().getDrawable(R.drawable.kindergarten)) );//
		avaible_types.add(new POIType("School",new HashMap<String, String>() { { put("amenity","school"); } },getResources().getDrawable(R.drawable.school)) );//
		avaible_types.add(new POIType("University",new HashMap<String, String>() { { put("amenity","university"); } },getResources().getDrawable(R.drawable.university)) );//
	    
		avaible_types.add(new POIType("Arts centre",new HashMap<String, String>() { { put("amenity","arts_centre"); }},getResources().getDrawable(R.drawable.artscentre)) );//
		avaible_types.add(new POIType("Artwork",new HashMap<String, String>() { { put("tourism","artwork"); } },getResources().getDrawable(R.drawable.artscentre) ) );//
		avaible_types.add(new POIType("Cinema",new HashMap<String, String>() { { put("amenity","cinema"); } },getResources().getDrawable(R.drawable.cinema)) );//
		avaible_types.add(new POIType("Library",new HashMap<String, String>() { { put("amenity","library"); }},getResources().getDrawable(R.drawable.library) ) );//
		avaible_types.add(new POIType("Museum",new HashMap<String, String>() { { put("tourism","museum"); } },getResources().getDrawable(R.drawable.museum)) );//
		avaible_types.add(new POIType("Nightclub",new HashMap<String, String>() { { put("amenity","nightclub"); }},getResources().getDrawable(R.drawable.nightclub)) );//
		avaible_types.add(new POIType("Theatre",new HashMap<String, String>() { { put("amenity","theatre"); }},getResources().getDrawable(R.drawable.theater)) );//
		avaible_types.add(new POIType("Theme Park",new HashMap<String, String>() { { put("amenity","theme_park"); } },getResources().getDrawable(R.drawable.themepark)) );//
		avaible_types.add(new POIType("Water park",new HashMap<String, String>() { { put("amenity","water_park"); } },getResources().getDrawable(R.drawable.waterpark)) );//
		avaible_types.add(new POIType("Zoo",new HashMap<String, String>() { { put("tourism","zoo"); } },getResources().getDrawable(R.drawable.zoo)) );//
		
		avaible_types.add(new POIType("Bakery",new HashMap<String, String>() { { put("shop","bakery"); } },getResources().getDrawable(R.drawable.bakery)) );//
		avaible_types.add(new POIType("Cafe",new HashMap<String, String>() { { put("amenity","cafe"); } },getResources().getDrawable(R.drawable.cafe)) );//
		avaible_types.add(new POIType("Fast Food",new HashMap<String, String>() { { put("amenity","fast_food"); } },getResources().getDrawable(R.drawable.fastfood)) );//
		avaible_types.add(new POIType("Pub",new HashMap<String, String>() { { put("amenity","pub"); } },getResources().getDrawable(R.drawable.pub)) );//
		avaible_types.add(new POIType("Restaurant",new HashMap<String, String>() { { put("amenity","restaurant"); } },getResources().getDrawable(R.drawable.restaurant)) );//
		avaible_types.add(new POIType("Bar",new HashMap<String, String>() { { put("amenity","bar"); } },getResources().getDrawable(R.drawable.bar)) );//
	    
		avaible_types.add(new POIType("Dentist",new HashMap<String, String>() { { put("helthcare","dentist"); }},getResources().getDrawable(R.drawable.dentist) ) );//
		avaible_types.add(new POIType("Doctor",new HashMap<String, String>() { { put("amenity","doctors"); } },getResources().getDrawable(R.drawable.doctor)) );//
		avaible_types.add(new POIType("Hospital",new HashMap<String, String>() { { put("amenity","hospital"); }},getResources().getDrawable(R.drawable.hospital)) );//
		avaible_types.add(new POIType("Pharmacy",new HashMap<String, String>() { { put("amenity","pharmacy"); }},getResources().getDrawable(R.drawable.pharmacy)) );//
		avaible_types.add(new POIType("Veterinary",new HashMap<String, String>() { { put("amenity","veterinary"); }},getResources().getDrawable(R.drawable.veterinary) ) ); //
		
		avaible_types.add(new POIType("Adult",new HashMap<String, String>() { { put("shop","erotic"); } },getResources().getDrawable(R.drawable.erotic)) );//
		avaible_types.add(new POIType("Alcohol",new HashMap<String, String>() { { put("shop","alcohol"); } },getResources().getDrawable(R.drawable.alcohol)) );//
		avaible_types.add(new POIType("Bicycle",new HashMap<String, String>() { { put("shop","bicycle"); } },getResources().getDrawable(R.drawable.bicycle)) );//
		avaible_types.add(new POIType("Books",new HashMap<String, String>() { { put("shop","books"); } },getResources().getDrawable(R.drawable.book) ));//
		avaible_types.add(new POIType("Butcher",new HashMap<String, String>() { { put("shop","butcher"); } },getResources().getDrawable(R.drawable.butcher)) );//
		avaible_types.add(new POIType("Clothes",new HashMap<String, String>() { { put("shop","clothes"); } },getResources().getDrawable(R.drawable.clothes)) );//
		avaible_types.add(new POIType("Computers",new HashMap<String, String>() { { put("shop","computer"); } },getResources().getDrawable(R.drawable.computer)) );//
		avaible_types.add(new POIType("Convinience",new HashMap<String, String>() { { put("shop","convenience"); } },getResources().getDrawable(R.drawable.convenience)) );//
		avaible_types.add(new POIType("Electronics",new HashMap<String, String>() { { put("shop","electronics"); } },getResources().getDrawable(R.drawable.electronics)) );//
		avaible_types.add(new POIType("Florist",new HashMap<String, String>() { { put("shop","florist"); } },getResources().getDrawable(R.drawable.florist)) );//
		avaible_types.add(new POIType("Furniture",new HashMap<String, String>() { { put("shop","furniture"); } },getResources().getDrawable(R.drawable.furniture)) );//
		avaible_types.add(new POIType("Sports",new HashMap<String, String>() { { put("shop","sports"); } },getResources().getDrawable(R.drawable.sports)) );//
		avaible_types.add(new POIType("Jewelers",new HashMap<String, String>() { { put("shop","jewelry"); } },getResources().getDrawable(R.drawable.jewelry)) );//
		avaible_types.add(new POIType("Mobile phones",new HashMap<String, String>() { { put("shop","mobile_phone"); } },getResources().getDrawable(R.drawable.mobilephone)) );//
		avaible_types.add(new POIType("Music",new HashMap<String, String>() { { put("shop","music"); } },getResources().getDrawable(R.drawable.music)) );//
		avaible_types.add(new POIType("Outdoor",new HashMap<String, String>() { { put("shop","outdoor"); } },getResources().getDrawable(R.drawable.outdoor)) );//
		avaible_types.add(new POIType("Photo",new HashMap<String, String>() { { put("shop","photo"); } },getResources().getDrawable(R.drawable.photo)) );//
		avaible_types.add(new POIType("Shoes",new HashMap<String, String>() { { put("shop","shoes"); } },getResources().getDrawable(R.drawable.shoes)) );//
		avaible_types.add(new POIType("Sovenirs",new HashMap<String, String>() { { put("shop","gift"); } },getResources().getDrawable(R.drawable.gift)) );//
		avaible_types.add(new POIType("Stationery",new HashMap<String, String>() { { put("shop","stationery"); } },getResources().getDrawable(R.drawable.stationery)) );//
		avaible_types.add(new POIType("Supermarket",new HashMap<String, String>() { { put("shop","supermarket"); } },getResources().getDrawable(R.drawable.supermarket)) );//
		avaible_types.add(new POIType("Toys",new HashMap<String, String>() { { put("shop","toys"); } },getResources().getDrawable(R.drawable.toys)) );//
	
		avaible_types.add(new POIType("Archery",new HashMap<String, String>() { { put("sport","archery"); } },getResources().getDrawable(R.drawable.archery)) );//
		avaible_types.add(new POIType("Athletics",new HashMap<String, String>() { { put("sport","athletics"); } },getResources().getDrawable(R.drawable.athletics)) );//
		avaible_types.add(new POIType("Baseball",new HashMap<String, String>() { { put("sport","baseball"); } },getResources().getDrawable(R.drawable.baseball)) );//
		avaible_types.add(new POIType("Basketball",new HashMap<String, String>() { { put("sport","basketball"); }},getResources().getDrawable(R.drawable.basketball)) );//
		avaible_types.add(new POIType("Boules",new HashMap<String, String>() { { put("sport","boules"); } },getResources().getDrawable(R.drawable.boule)) );//
		avaible_types.add(new POIType("Bowls",new HashMap<String, String>() { { put("sport","bowls"); } },getResources().getDrawable(R.drawable.bowls)) );//
		avaible_types.add(new POIType("Caone",new HashMap<String, String>() { { put("sport","canoe"); } },getResources().getDrawable(R.drawable.canoe)) );//
		avaible_types.add(new POIType("Climbing",new HashMap<String, String>() { { put("sport","climbing"); } },getResources().getDrawable(R.drawable.hillclimbing)) );//
		avaible_types.add(new POIType("Equestrian",new HashMap<String, String>() { { put("sport","equestrian"); } },getResources().getDrawable(R.drawable.equestrian)) );//
		avaible_types.add(new POIType("Motor sport",new HashMap<String, String>() { { put("sport","motor"); } },getResources().getDrawable(R.drawable.motorbike)) );//
		avaible_types.add(new POIType("Multi",new HashMap<String, String>() { { put("sport","multi"); } },getResources().getDrawable(R.drawable.multi)) );//
		avaible_types.add(new POIType("Pitch",new HashMap<String, String>() { { put("leisure","pitch"); } },getResources().getDrawable(R.drawable.pitch)) );//
		avaible_types.add(new POIType("Rugby",new HashMap<String, String>() { { put("sport","rugby_union"); } },getResources().getDrawable(R.drawable.football)) );//
		avaible_types.add(new POIType("Shooting",new HashMap<String, String>() { { put("sport","shooting"); } },getResources().getDrawable(R.drawable.shooting)) );//
		avaible_types.add(new POIType("Skateboard",new HashMap<String, String>() { { put("sport","skateboard"); } },getResources().getDrawable(R.drawable.skateboard)) );//
		avaible_types.add(new POIType("Skating",new HashMap<String, String>() { { put("sport","skating"); } },getResources().getDrawable(R.drawable.skating)) );//
		avaible_types.add(new POIType("Soccer",new HashMap<String, String>() { { put("sport","soccer"); } },getResources().getDrawable(R.drawable.soccer)) );//
		avaible_types.add(new POIType("Sport Center",new HashMap<String, String>() { { put("leisure","sports_centre"); } },getResources().getDrawable(R.drawable.sportscentre)) );//
		avaible_types.add(new POIType("Stadium",new HashMap<String, String>() { { put("leisure","stadium"); } },getResources().getDrawable(R.drawable.stadium)) );//
		avaible_types.add(new POIType("Swiming",new HashMap<String, String>() { { put("sport","swiming"); } },getResources().getDrawable(R.drawable.swimming)) );//
		avaible_types.add(new POIType("Table Tenis",new HashMap<String, String>() { { put("sport","table_tenis"); } },getResources().getDrawable(R.drawable.tabletennis)) );//
		avaible_types.add(new POIType("Tenis",new HashMap<String, String>() { { put("sport","tenis"); } },getResources().getDrawable(R.drawable.tennis)) );//
		avaible_types.add(new POIType("Track",new HashMap<String, String>() { { put("leisure","track"); } },getResources().getDrawable(R.drawable.track)) );//
		
		avaible_types.add(new POIType("Bicycle parking",new HashMap<String, String>() { { put("amenity","bicycle_parking"); } },getResources().getDrawable(R.drawable.bikeparking)) );//
		avaible_types.add(new POIType("Bicycle Rental",new HashMap<String, String>() { { put("amenity","bicycle_rental"); } },getResources().getDrawable(R.drawable.bicyclerental)) );//
		avaible_types.add(new POIType("Bus stop",new HashMap<String, String>() { { put("highway","bus_stop"); } },getResources().getDrawable(R.drawable.bus)) );//
		avaible_types.add(new POIType("Taxi stop",new HashMap<String, String>() { { put("amenity","taxi"); } },getResources().getDrawable(R.drawable.taxi)) );//
		avaible_types.add(new POIType("Train station",new HashMap<String, String>() { { put("building","train_station"); } },getResources().getDrawable(R.drawable.train)) );//
		avaible_types.add(new POIType("Tram stop",new HashMap<String, String>() { { put("railway","tram_stop"); } },getResources().getDrawable(R.drawable.tram)) );//



		this.mode_add_poi=false;
	    
		ExpandableListAdapter ExpLstAdapter = new ExpandableListAdapter(this, filt);
		
		drawerlist.setAdapter(ExpLstAdapter);
		
		
		this.load_state();
		
		
		drawerlist.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int position, long id) {
				setTypeOfNewPOI(filt.getType(groupPosition,position));
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
		if (drawertoggle.onOptionsItemSelected(item))
		{
			return true;
		}
		if (id == R.id.action_settings) {
			this.openSettingsActivity();
            //DialogHour d=new DialogHour(MainActivity.this);
            //d.show();
            /*d.onOK(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("value", ((Button)v).getTag().toString() );
                }
            });*/



			return true;
		}
		else if(id==R.id.action_locate)
		{
			this.center_map_to_location();
			return true;
		}
		else if(id==R.id.action_delete_marker)
		{
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
		if (suc)
		{
			Log.v("ui","Removed succesfuly");
		}
		else
		{
			Log.v("ui","Not removed");
		}
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
			//map.getController().setZoom(this.getLastZoom());
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

		this.selected_marker=marker;
		this.showValues();
		
		if (p.getValue("name")!=null)
		{
			((EditText)this.findViewById(R.id.etName)).setText(p.getValue("name"));	
		}
		else
		{
			((EditText)this.findViewById(R.id.etName)).setText("");
		}
		if(p.getValue("address")!=null)
		{
			((EditText)this.findViewById(R.id.etAdress)).setText(p.getValue("adress"));
		}
		else
		{
			((EditText)this.findViewById(R.id.etAdress)).setText("");
		}
		if(p.getValue("website")!=null)
		{
			((EditText)this.findViewById(R.id.etWeb)).setText(p.getValue("website"));
		}
		else
		{
			((EditText)this.findViewById(R.id.etWeb)).setText("");	
		}
		if(p.getValue("phone")!=null)
		{
			((EditText)this.findViewById(R.id.etPhone)).setText(p.getValue("phone"));
		}
		else
		{
			((EditText)this.findViewById(R.id.etPhone)).setText("");	
		}
		if(p.getValue("opening_hours")!=null)
		{
			((EditText)this.findViewById(R.id.etHours)).setText(p.getValue("opening_hours"));
		}
		else
		{
			((EditText)this.findViewById(R.id.etHours)).setText("");
		}
		if(p.getValue("description")!=null)
		{
			((EditText)this.findViewById(R.id.etDescription)).setText(p.getValue("description"));
		}
		else
		{
			((EditText)this.findViewById(R.id.etDescription)).setText("");
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
				
				m.setIcon(this.getResources().getDrawable(R.drawable.ic_launcher));
			}
			else
			{
				m.setIcon(p.getType().getIcon());
			}
			
		}
		m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker, MapView mapView) {
				Log.v("bd","open");
				BD bd=new BD(mapView.getContext());
				POI p=bd.getPOI(Long.valueOf(marker.getTitle()),((MainActivity)mapView.getContext()).getAvaibleTypes());
				Log.v("bd","close");
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
	            
	            if (name!=null)
	            {
	            	tags.put("name",name );
	            }
	            if (address!=null)
	            {
	
	            	tags.put("address",address);
	            }
	            if (description!=null)
	            {
	            	tags.put("description",description);
	            }
	            if (opening_hours!=null)
	            {
	            	tags.put("opening_hours",opening_hours);
	            }
	            if (phone!=null)
	            {
	            	tags.put("phone",phone);
	            }
	            if (web!=null)
	            {
	            	tags.put("web",web);
	            }
	            Log.v("bd","open");
	            BD bd=new BD(getApplicationContext());
	            Long id=bd.getFreeId();
	            POI p=new POI(lat,lon,tags, id, null,null,1);
	            p.setType(this.newPOI_type);
	            //p.detect_type(avaible_types);
	            bd.addPOI(p, "create");
	            
	            GeoPoint gp = new GeoPoint(lat, lon,0 );
		        
	            layerPOIs.addItem(new OverlayItem(name, description, gp));
	            Log.v("bd", "close");
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
			//Bundle b=this.getActivity().getIntent().getExtras();
			Button save=(Button) rootView.findViewById(R.id.save);
			
			final EditText etName=(EditText)rootView.findViewById(R.id.etName);
			final EditText etAdress=(EditText)rootView.findViewById(R.id.etAdress);
			final EditText etDescription=(EditText)rootView.findViewById(R.id.etDescription);
			final EditText etHours=(EditText)rootView.findViewById(R.id.etHours);
			final EditText etPhone=(EditText)rootView.findViewById(R.id.etPhone);
			final EditText etWeb=(EditText)rootView.findViewById(R.id.etWeb);

            etHours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(final View vi, boolean hasFocus) {
                    if (hasFocus)
                    {
                        DialogHour d = new DialogHour(vi.getContext(), ((EditText) vi).getText().toString());
                        d.onOK(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((EditText) vi).setText((String) v.getTag());
                            }
                        });
                        d.show();
                    }
                }
            });

			save.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					HashMap<String,String> values=new HashMap<String,String>();
					
					if (! etName.getText().toString().equalsIgnoreCase(""))
					{
						values.put("name", etName.getText().toString());
						etName.setText("");
					}
					if (! etAdress.getText().toString().equalsIgnoreCase(""))
					{
						values.put("address", etAdress.getText().toString());
						etAdress.setText("");
					}
					if (! etDescription.getText().toString().equalsIgnoreCase(""))
					{
						values.put("description",etDescription.getText().toString());
						etDescription.setText("");
					}
					if (! etHours.getText().toString().equalsIgnoreCase(""))
					{
						values.put("opening_hours",etHours.getText().toString());
						etHours.setText("");
					}
					if (! etPhone.getText().toString().equalsIgnoreCase(""))
					{
						values.put("phone", etPhone.getText().toString());
						etPhone.setText("");
					}
					if (! etWeb.getText().toString().equalsIgnoreCase(""))
					{
						values.put("web", etWeb.getText().toString());
						etWeb.setText("");
					}
					mCallback.setValuesOfNewPOI(values);
					mCallback.hideValues();
					mCallback.createNewPOI();
				}
			});
			
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
	
	public void follow()
	{
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,bll);
		
	}
	public void unfollow()
	{
		locationmanager.removeUpdates(bll);
	}
	public void showValues()
	{
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
	protected void setLatLonOfNewPOI(double lat,double lon)
	{
		this.newPOI_lat=lat;
		this.newPOI_lon=lon;
	}
	protected void setValuesOfNewPOI(HashMap<String, String> values) {
		this.newPOI_values=values;
	}
	protected void setTypeOfNewPOI(POIType type)
	{
		this.newPOI_type=type;
	}

	protected void createNewPOI()
	{
		MapView map=(MapView)this.findViewById(R.id.mapview);
		BD bd=new BD(this);
		Log.v("bd","open");
        Long id=bd.getFreeId();
        POI p=new POI(this.newPOI_lat,this.newPOI_lon,this.newPOI_values, id, null,null,1);
        p.setType(this.newPOI_type);

        Log.v("bd", "id new poi->"+String.valueOf(id));

        bd.addPOI(p, "created");
        Log.v("bd", "close");
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
		this.newPOI_type=filt.getType(groupName, second);
	}
	public void changeFilter(String groupName, Integer second, boolean checked) {
		
		POIType t=filt.getType(groupName, second);
		
		if (checked)
		{
			filt.enableType(t);

		}
		else
		{
			filt.disableType(t);			
		}
	}
	public List<POIType> getAvaibleTypes()
	{
		return this.avaible_types;
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		
		Log.v("Drag", "Mig");
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		MapView map=((MapView)this.findViewById(R.id.mapview));
		
		map.getOverlays().remove(this.marker_drag);
		//map.invalidate();
		Log.v("Drag", "End");
		map.getOverlays().add(marker);
		BD bd=new BD(this);
		Log.v("bd", "open");
		POI p= bd.getPOI(Long.valueOf(marker.getTitle()), filt.getTypes());
		p.setGeoPoint(marker.getPosition());
		Log.v("bd", "close");
		bd.updatePOI(p);
		bd.close();
		OSMAPI api=new OSMAPI(this);
		api.addPOI(filt.getTypes());
		map.invalidate();
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		Log.v("Drag", "Start");
		Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
		this.marker_drag=marker;
		
	}

	public void updateIds(List<POI> outdated_pois) {
		List<Overlay> overlay_list =((MapView)this.findViewById(R.id.mapview)).getOverlays();
		int x;
		Marker tmp;
		for (POI p:outdated_pois)
		{
			if( p.getStatus()=="created")
			{
				x=1;
				while (x<overlay_list.size() &&(Long.valueOf(((Marker)overlay_list.get(x)).getTitle())!=(-1*p.getId())))
				{
					x++;
				}
				if ((x<overlay_list.size()) && (Long.valueOf(((Marker)overlay_list.get(x)).getTitle())==(-1*p.getId())))
				{
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
	public static void end()
	{
		
		
	}
}

