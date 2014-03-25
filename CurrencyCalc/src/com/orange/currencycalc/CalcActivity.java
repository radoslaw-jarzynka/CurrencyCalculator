package com.orange.currencycalc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
/**
 * 
 * @author radoslawjarzynka
 * Glowne Activity otwierane po wlaczeniu aplikacji z interfejsem sluzacym do przeliczania walut
 */
public class CalcActivity extends Activity {
	
	Currency fromCurrency;
	Currency toCurrency;
	
	//do obslugi SQLite
	private DBAdapter dbAdapter;
	private Cursor currencyCursor;
	
	HashMap<String, Currency> currencies;
	
	//elementy interfejsu
	Button calculateBtn;
	Button refreshBtn;
	Button findBankBtn;
	EditText fromEdit;
	EditText toEdit;
	ProgressBar downloading;
	Spinner fromSpinner;
	Spinner toSpinner;
	
	/**
	 * Pobranie danych wszystkich walut z bazy danych
	 */
	private void getAllCurrencies() {
	    currencyCursor = getAllEntriesFromDb();
	    updateCurrencyMap();
	}
	 
	/**
	 * metoda ustawiajaca kursor na pierwszym elemencie bazy danych zawierajacej informacje o walutach
	 * @return kursor do bazy danych
	 */
	@SuppressWarnings("deprecation")
	private Cursor getAllEntriesFromDb() {
	    currencyCursor = dbAdapter.getAllCurrencies();
	    if(currencyCursor != null) {
	    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
	            startManagingCursor(currencyCursor);
	        }
	        currencyCursor.moveToFirst();
	    }
	    return currencyCursor;
	}
	 
	/**
	 * metoda tworzaca mape currencies na podstawie informacji zawartych w bazie danych
	 */
	private void updateCurrencyMap() {
	    if(currencyCursor != null && currencyCursor.moveToFirst()) {
	        do {
	        	String name = currencyCursor.getString(DBAdapter.NAME_COLUMN);
	        	double value = currencyCursor.getDouble(DBAdapter.VALUE_COLUMN);
	        	double multiplier = currencyCursor.getDouble(DBAdapter.MULTIPLIER_COLUMN);
	            currencies.put(name, new Currency(name,value,multiplier));
	        } while(currencyCursor.moveToNext());
	    }
	    updateSpinners();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		
		dbAdapter = new DBAdapter(getApplicationContext());
		dbAdapter.open();
		getAllCurrencies();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		dbAdapter = new DBAdapter(getApplicationContext());
		dbAdapter.open();
		getAllCurrencies();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calc);
		
  		currencies = new HashMap<String, Currency>();
  	    
		calculateBtn = (Button) findViewById(R.id.calcButton);
		refreshBtn = (Button) findViewById(R.id.refreshButton);
		findBankBtn = (Button) findViewById(R.id.findBankButton);
		downloading = (ProgressBar) findViewById(R.id.progressBar1);
		fromEdit = (EditText) findViewById(R.id.fromEdit);
		toEdit = (EditText) findViewById(R.id.toEdit);
		toSpinner = (Spinner) findViewById(R.id.toSpinner);
		fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
		
		dbAdapter = new DBAdapter(getApplicationContext());
		dbAdapter.open();
		getAllCurrencies();
		
		dbAdapter.close();
		
		// jezeli baza byla pusta - sciagamy informacje z internetu
		if (currencies.isEmpty()) {
			CurrencyDownloader currencyDownloader = new CurrencyDownloader(currencies);
			currencyDownloader.execute("http://www.nbp.pl/kursy/xml/LastA.xml");
		}
		else {
			downloading.setVisibility(View.GONE);
		}
		
		// dodanie Listenerow do poszczegolnych przyciskow i spinnerow
		
		calculateBtn.setOnClickListener(new OnClickListener() {
			
			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				if (!fromEdit.getText().toString().equals("")) {
					double fromValue = Double.parseDouble(fromEdit.getText().toString());
					if (toCurrency != null && fromCurrency != null && !fromCurrency.name.equals("PLN")) {
						//przerzucamy do PLN
						double inPLN = fromValue*(fromCurrency.value*fromCurrency.multiplier);
						//przerzucamy do wlasciwej waluty
						toEdit.setText(String.format("%,.2f", inPLN/(toCurrency.value*toCurrency.multiplier)));
					} else if (toCurrency != null) {
						toEdit.setText(String.format("%,.2f", fromValue/(toCurrency.value*toCurrency.multiplier)));
					}
				}
			}
		});
		
		toSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			/* (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				toCurrency = currencies.get(toSpinner.getItemAtPosition(arg2).toString());
				RadioGroup radioGroup = (RadioGroup) findViewById(R.id.toRadioGroup);
				radioGroup.check(R.id.toOther);
				
			}

			/* (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
			 */
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				toCurrency = null;	
				RadioGroup radioGroup = (RadioGroup) findViewById(R.id.toRadioGroup);
				radioGroup.clearCheck();
			}
		});
		
		fromSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			/* (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				fromCurrency = currencies.get(toSpinner.getItemAtPosition(arg2).toString());
				RadioGroup radioGroup = (RadioGroup) findViewById(R.id.fromRadioGroup);
				radioGroup.check(R.id.fromOther);
			}

			/* (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
			 */
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				fromCurrency = null;
				RadioGroup radioGroup = (RadioGroup) findViewById(R.id.fromRadioGroup);
				radioGroup.clearCheck();
			}
		});
		
		refreshBtn.setOnClickListener(new OnClickListener() {
			
			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				if (isOnline()) {
				CurrencyDownloader currencyDownloader = new CurrencyDownloader(currencies);
				currencyDownloader.execute("http://www.nbp.pl/kursy/xml/LastA.xml");
				downloading.setVisibility(View.VISIBLE);
				}
			}
		});
		
		findBankBtn.setOnClickListener(new OnClickListener() {
			
			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@SuppressWarnings("rawtypes")
			@Override
			public void onClick(View v) {
				if(dbAdapter != null) {
					dbAdapter.open();
					Iterator it = currencies.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry pairs = (Map.Entry)it.next();
						Currency _cur = (Currency) pairs.getValue();
						dbAdapter.insertCurrency(_cur);
						it.remove(); // avoids a ConcurrentModificationException
					}
					dbAdapter.close();
				}
				Intent i = new Intent(getApplicationContext(), MapActivity.class);
				startActivity(i);
			}
		});
		
		
	}


	/**
	 * metoda ustawiajaca obiekt fromCurrency na podstawie wcisnietego radiobuttona
	 * @param view wcisniety radiobutton
	 */
	public void onFromRadioButtonClicked(View view) {
		RadioButton radioButton = (RadioButton) view;
		String other = getResources().getString(R.string.other);
		if (radioButton.getText().equals(other)) {
			fromCurrency = currencies.get(fromSpinner.getSelectedItem().toString());
		} else fromCurrency = currencies.get(radioButton.getText());
	}
	/**
	 * metoda ustawiajaca obiekt toCurrency na podstawie wcisnietego radiobuttona
	 * @param view wcisniety radiobutton
	 */
	public void onToRadioButtonClicked(View view) {
		RadioButton radioButton = (RadioButton) view;
		String other = getResources().getString(R.string.other);
		if (radioButton.getText().equals(other)) {
			toCurrency = currencies.get(fromSpinner.getSelectedItem().toString());
		} else toCurrency = currencies.get(radioButton.getText());
	}
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.calc, menu);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId()==R.id.action_about){
			//Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
			//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//getApplicationContext().startActivity(intent);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	/**
	 * metoda uaktualniajaca zawartosc spinnerow (list drop-down) na podstawie kluczy w mapie currencies
	 */
	private void updateSpinners() {
		if (currencies != null) {
			List<String> list = new ArrayList<String>();
			for (String s : currencies.keySet()) {
				if (!s.equals("PLN") && !s.equals("EUR") && !s.equals("GBP") && !s.equals("USD")) {
					list.add(s);
				}
				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				fromSpinner.setAdapter(dataAdapter);
				toSpinner.setAdapter(dataAdapter);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected void onStop() {
		super.onStop();
		if(dbAdapter != null) {
			dbAdapter.open();
			Iterator it = currencies.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry)it.next();
				Currency _cur = (Currency) pairs.getValue();
				//if (dbAdapter.doesCurrencyExistInDB(_cur.name)) dbAdapter.updateCurrency(_cur);
				//else 
				dbAdapter.insertCurrency(_cur);
				it.remove(); // avoids a ConcurrentModificationException
			}
			dbAdapter.close();
		}
		
	}
	
	/**
	 * metoda sprawdzajaca, czy aplikacja ma dostep do internetu
	 * @return true gdy jest dostep, false gdy go nie ma
	 */
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}


	/**
	 *	Klasa obslugujaca sciaganie kursow walut w innym watku niz aplikacja
	 */
	private class CurrencyDownloader extends AsyncTask<String, Void, HashMap<String, Currency>> {
		
		HashMap<String,Currency> currencies;
		
		/**
		 * konstruktor
		 * @param currencies mapa, do ktorej bedziemy zapisywac wyniki
		 */
		public CurrencyDownloader(HashMap<String,Currency> currencies) {
			this.currencies = currencies;
		}
		
	    /* (non-Javadoc)
	     * @see android.os.AsyncTask#doInBackground(Params[])
	     */
	    protected HashMap<String, Currency> doInBackground(String... url) {
	    	//downloading.setVisibility(View.VISIBLE);
	    	Log.d("", "Downlading!");
	    	return CurrenciesParser.getCurrencies(url[0], currencies);
	    }
	    
	    /* (non-Javadoc)
	     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	     */
	    protected void onPostExecute(HashMap<String, Currency> result) {
	    	currencies = result;
	    	updateSpinners();
	    	Log.d("", "Finished Downloading!");
	    	downloading.setVisibility(View.GONE);
	    }
	}

}
