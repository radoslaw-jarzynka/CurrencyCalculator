package currencycalc;


import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.orange.currencycalc.R;


/**
 * @author radoslawjarzynka
 *	Activity obslugujace wyswietlanie na mapie najblizszych bankow
 */
public class MapActivity extends FragmentActivity implements LocationListener {

	// zmienne do wyswietlania mapy z odpowiednim przyblizeniem i zasieg poszukiwania bankow
	public static int ZOOM_LVL = 16;
	public static int RADIUS = 1000;
	
	//ta zmienna zabezpiecza przed tym, by przy kazdym kolejnym evencie "locationchanged" nie wysylalo na nowo zadania znalezienia najblizszych bankow
	//aplikacja robic to bedzie tylko na zadanie uzytkownika
	private boolean firstLocationChange;
	
	
	//elementy interfejsu
	private TextView latituteField;
	private TextView longitudeField;
	private Button refreshButton;
	private LocationManager locationManager;
	private GoogleMap mMap;
	
	//szerokosci i dlugosc geograficzna uzytkownika
	private double lat;
	private double lng;
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//domyslna lokalizacja to centrum warszawy
		lat = 52.2297700;
		lng = 21.0117800;
		
		firstLocationChange = true;
		
		//ustawianie elementow interfejsu
		setContentView(R.layout.activity_map);
		latituteField = (TextView) findViewById(R.id.TextView02);
	    longitudeField = (TextView) findViewById(R.id.TextView04);
	    latituteField.setText(String.valueOf(lat));
	    longitudeField.setText(String.valueOf(lng));
	    refreshButton = (Button) findViewById(R.id.refreshButton);
	    
	    
	    //dodawanie listenerow
	    refreshButton.setOnClickListener(new OnClickListener() {

			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View arg0) {
				setUpMapIfNeeded();
			}
	    	
	    });
	    
	    //zadanie otrzymywania informacji o zmianie polozenia
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    if (LocationManager.NETWORK_PROVIDER != null) {
	    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);           
	    } 	    
	    
	    //stworzenie elementu mapy i dodanie listenera obslugujacego klikniecie w marker
	    mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				marker.showInfoWindow();
				return false;
			}
	    	
	    });
	    
	    //wywolanie metody obslugujacej mape
	    setUpMapIfNeeded();
	    
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	  protected void onResume() {
	    super.onResume();
	    if (LocationManager.GPS_PROVIDER != null) {
	    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
	    }
	  }
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Intent i = new Intent(getApplicationContext(), CalcActivity.class);
		startActivity(i);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
	    super.onPause();
	    locationManager.removeUpdates(this);
	}

	/**
	 * metoda obslugujaca rysowanie mapy
	 */
	protected void setUpMapIfNeeded() {
		//sprawdzenie czy obiekt mapy nie jest nullem, jak jest to utworzenie go
	    if (mMap == null) {
	        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	        if (mMap != null) {
	        	//jezeli mMap bylo null i utworzylo nowy obiekt w jego miejsce niech wykona te same operacje, ktore wykonalyby sie gdyby mMap nie byl null na poczatku
	        	mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	        	if (!mMap.isMyLocationEnabled()) mMap.setMyLocationEnabled(true);
	        	CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(lat,lng));
	        	CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZOOM_LVL);
	        	mMap.moveCamera(center);
	        	mMap.animateCamera(zoom);
	        	mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
						.position(new LatLng(lat,lng)));
	        }
	    } else {
	    	//ustawienie kamery i typu mapy
	    	mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	    	CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(lat,lng));
        	CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZOOM_LVL);
        	mMap.moveCamera(center);
        	mMap.animateCamera(zoom);
        	//dodanie markeru z wlasna pozycja
        	mMap.addMarker(new MarkerOptions().title(getResources().getString(R.string.youAreHere))
        			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
									.position(new LatLng(lat,lng)));
        	//wyslanie zadania sprawdzenia lokalizacji najblizszych bankow
    		BankDownloader bd = new BankDownloader();
    		bd.execute();

	    }
	}
	
	
	/* (non-Javadoc)
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
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

	/* (non-Javadoc)
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	public void onStatusChanged(String provider, int status, Bundle extras) {
	    // TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
	   // Toast.makeText(this, "Enabled new provider " + provider,Toast.LENGTH_SHORT).show();

	}

	/* (non-Javadoc)
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
	 //   Toast.makeText(this, "Disabled provider " + provider,Toast.LENGTH_SHORT).show();
	}
	
	
	/**
	 * @author radoslawjarzynka
	 * klasa obslugujaca wyslanie zadania do Google Places API i obsluzenie odpowiedzi
	 */
	private class BankDownloader extends AsyncTask<Void, Void, BankList> {

		private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
		private static final String API_KEY = "AIzaSyAnYyGWvaPf06kETYG_Qox1gAmOhyKUkzw";
		//odpowiedz wysylana jest w formacie json
	    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";

		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected BankList doInBackground(Void... args) {
			try {
			Log.d("map", "Looking up for addresses!");
			//generowanie zadania
			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("name", "bank");
            request.getUrl().put("location", lat + "," + lng);
            request.getUrl().put("radius", RADIUS); // in meters
            request.getUrl().put("sensor", "false");
            //parsowanie odpowiedzi z JSON na obiekt BankList
            BankList list = request.execute().parseAs(BankList.class);            
			Log.d("map", "Finished looking up for addresses!");
			return list;
			} catch (Exception e) {
				//jakby byly bledy niech zwroci pusta liste - nie bedzie null pointerow
				BankList bl = new BankList();
				return bl;
			}
		}
		
		/**
		 * @param transport obiekt obslugujacy transport po protokole http
		 * @return fabryka obiektow - zadan http
		 */
		public HttpRequestFactory createRequestFactory(final HttpTransport transport) { 
	        return transport.createRequestFactory(new HttpRequestInitializer() {
	            public void initialize(HttpRequest request) {
	                HttpHeaders headers = new HttpHeaders();
	                request.setHeaders(headers);
	                JsonObjectParser parser = new JsonObjectParser(new JacksonFactory());
	                request.setParser(parser);
	            }
	        });
	    }
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(BankList bl) {
			if (bl!= null) {
				if (bl.results.size() != 0) {
					//dla kazdego banku dodaj marker z jego adresem na mapie
					for (Bank bank : bl.results) {
						mMap.addMarker(new MarkerOptions().title(bank.name).snippet(bank.vicinity)
										.position(new LatLng(bank.geometry.location.lat, bank.geometry.location.lng)));
					}
				}
			}
		}
		
	}
	
}
