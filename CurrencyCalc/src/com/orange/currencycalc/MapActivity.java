package com.orange.currencycalc;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapActivity extends FragmentActivity implements LocationListener {

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
	    if (LocationManager.GPS_PROVIDER != null) {
	    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);           
	    } 	    
	    mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
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
	        	CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
	        	mMap.moveCamera(center);
	        	mMap.animateCamera(zoom);
	        }
	    } else {
	    	CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(lat,lng));
        	CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
        	mMap.moveCamera(center);
        	mMap.animateCamera(zoom);
        	
        	
        	
	    }
	}
	
	
	@Override
	public void onLocationChanged(Location location) {
	    lat = (double) (location.getLatitude());
	    lng = (double) (location.getLongitude());
	    latituteField.setText(String.valueOf(lat).substring(0, 9));
	    longitudeField.setText(String.valueOf(lng).substring(0, 9));
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
}
