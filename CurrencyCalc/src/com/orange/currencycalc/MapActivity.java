package com.orange.currencycalc;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;


public class MapActivity extends FragmentActivity implements LocationListener {

	public static int ZOOM_LVL = 17;
	public static int RADIUS = 1000;
	
	
	
	private TextView latituteField;
	private TextView longitudeField;
	private Button refreshButton;
	private LocationManager locationManager;
	private GoogleMap mMap;
	private double lat;
	private double lng;
	
	private boolean firstLocationChange;
	
	private class ButtonClickListener implements OnClickListener {

		private MapActivity mA;
		public ButtonClickListener(MapActivity m) {
			this.mA = m;
		}

		@Override
		public void onClick(View v) {
	        mA.setUpMapIfNeeded();
		}

	};
	
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		lat = 52.2297700;
		lng = 21.0117800;

		firstLocationChange = true;
		setContentView(R.layout.activity_map);
		latituteField = (TextView) findViewById(R.id.TextView02);
	    longitudeField = (TextView) findViewById(R.id.TextView04);
	    latituteField.setText(String.valueOf(lat));
	    longitudeField.setText(String.valueOf(lng));
	    refreshButton = (Button) findViewById(R.id.refreshButton);
	    
	    refreshButton.setOnClickListener(new ButtonClickListener(this));
	    
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    if (LocationManager.NETWORK_PROVIDER != null) {
	    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);           
	    } 	    
	    mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				marker.showInfoWindow();
				return false;
			}
	    	
	    });
	    setUpMapIfNeeded();
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	@Override
	  protected void onResume() {
	    super.onResume();
	    if (LocationManager.GPS_PROVIDER != null) {
	    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
	    }
	  }
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(getApplicationContext(), CalcActivity.class);
		startActivity(i);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    locationManager.removeUpdates(this);
	}

	protected void setUpMapIfNeeded() {
	    if (mMap == null) {
	        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	        if (mMap != null) {
	        	mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	        	if (!mMap.isMyLocationEnabled()) mMap.setMyLocationEnabled(true);
	        	CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(lat,lng));
	        	CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZOOM_LVL);
	        	mMap.moveCamera(center);
	        	mMap.animateCamera(zoom);
	        }
	    } else {
	    	CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(lat,lng));
        	CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZOOM_LVL);
        	mMap.moveCamera(center);
        	mMap.animateCamera(zoom);
        	//String[] args = new String[2];
        	//args[0] = "" + lat;
        	//args[1] = "" + lng;
        	BankDownloader bd = new BankDownloader();
        	bd.execute();
	    }
	}
	
	
	@Override
	public void onLocationChanged(Location location) {
	    lat = (double) (location.getLatitude());
	    lng = (double) (location.getLongitude());
	    if (String.valueOf(lat).length() >= 10) {
	    	latituteField.setText(String.valueOf(lat).substring(0, 9));
	    } else {
	    	latituteField.setText(String.valueOf(lat));
	    }
	    if (String.valueOf(lng).length() >= 10) {
	    	longitudeField.setText(String.valueOf(lng).substring(0, 9));
	    } else {
	    	longitudeField.setText(String.valueOf(lng));
	    }
	    if (firstLocationChange) {
	    	setUpMapIfNeeded();
	    	firstLocationChange = false;
	    }
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	    // TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
	   // Toast.makeText(this, "Enabled new provider " + provider,Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
	 //   Toast.makeText(this, "Disabled provider " + provider,Toast.LENGTH_SHORT).show();
	}
	
	
	private class BankDownloader extends AsyncTask<Void, Void, BankList> {

		private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
		private static final String API_KEY = "AIzaSyAnYyGWvaPf06kETYG_Qox1gAmOhyKUkzw";
	    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	    private static final String PLACES_TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	    private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

		
		@Override
		protected BankList doInBackground(Void... args) {
			try {
			Log.d("map", "Looking up for addresses!");
			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("name", "bank");
            request.getUrl().put("location", lat + "," + lng);
            request.getUrl().put("radius", RADIUS); // in meters
            request.getUrl().put("sensor", "false");
            //if(types != null)
              //  request.getUrl().put("types", types);
            
            BankList list = request.execute().parseAs(BankList.class);
            // Check log cat for places response status
            
			Log.d("map", "Finished looking up for addresses!");
			return list;
			} catch (Exception e) {
				//jakby byly bledy niech zwroci pusta liste - nie bedzie null pointerow
				BankList bl = new BankList();
				return bl;
			}
		}
		
		public HttpRequestFactory createRequestFactory(final HttpTransport transport) { 
	        return transport.createRequestFactory(new HttpRequestInitializer() {
	            public void initialize(HttpRequest request) {
	                //GoogleHeaders headers = new GoogleHeaders();
	                HttpHeaders headers = new HttpHeaders();
	                //headers.setApplicationName("currencyCalc");
	                request.setHeaders(headers);
	                
	                JsonObjectParser parser = new JsonObjectParser(new JacksonFactory());
	                //request.addParser(parser);
	                request.setParser(parser);
	            }
	        });
	    }
		
		@Override
		protected void onPostExecute(BankList bl) {
			if (bl.results.size() != 0) {
				for (Bank bank : bl.results) {
					mMap.addMarker(new MarkerOptions().title(bank.name).snippet(bank.vicinity)
									.position(new LatLng(bank.geometry.location.lat, bank.geometry.location.lng)));
									
									
				}
			} else {
				Toast.makeText(getApplicationContext(), "Nie znaleziono bankow w poblizu, rozszerzam zakres przeszukiwania", Toast.LENGTH_SHORT);
				//ZOOM_LVL--;
				//setUpMapIfNeeded();
			}
		}
		
	}
	
}
