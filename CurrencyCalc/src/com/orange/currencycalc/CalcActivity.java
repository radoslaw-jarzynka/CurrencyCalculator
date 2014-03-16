package com.orange.currencycalc;

import java.text.Format;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;

public class CalcActivity extends Activity {
	
	public static final String PREFS_NAME = "CurrancyCalcFile";
	
	Currency fromCurrency;
	Currency toCurrency;
	
	HashMap<String, Currency> currencies;
	Button calculateBtn;
	Button refreshBtn;
	EditText fromEdit;
	EditText toEdit;
	ProgressBar downloading;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calc);
		
  		currencies = new HashMap<String, Currency>();
//		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
//		
//		int numberOfEntries = prefs.getInt("numberOfEntries", 0);
//		if (numberOfEntries != 0) {
//			for (int i = 0; i < numberOfEntries; i++) {
//				String str = prefs.getString(""+i, "");
//				if (!str.equals("")) {
//					String _curStr = prefs.getString(""+i, "");
//					if (!_curStr.equals("")) {
//						Currency _cur = Currency.getCurrencyFromSerializedString(_curStr);
//						currencies.put(_cur.name, _cur);
//					}
//				}
//			}
//		}
		
		CurrencyDownloader currencyDownloader = new CurrencyDownloader();
		currencyDownloader.execute("http://www.nbp.pl/kursy/xml/LastA.xml");
		
		calculateBtn = (Button) findViewById(R.id.calcButton);
		refreshBtn = (Button) findViewById(R.id.refreshButton);
		downloading = (ProgressBar) findViewById(R.id.progressBar1);
		fromEdit = (EditText) findViewById(R.id.fromEdit);
		toEdit = (EditText) findViewById(R.id.toEdit);
		
		calculateBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				double fromValue = Double.parseDouble(fromEdit.getText().toString());
				if (!fromCurrency.name.equals("PLN") && toCurrency != null && fromCurrency != null) {
					//przerzucamy do PLN
					double inPLN = fromValue/(fromCurrency.value*fromCurrency.multiplier);
					//przerzucamy do wlasciwej waluty
					toEdit.setText(String.format("%,.2f", inPLN*(toCurrency.value*toCurrency.multiplier)));
				} else if (toCurrency != null) {
					toEdit.setText(String.format("%,.2f", fromValue*(toCurrency.value*toCurrency.multiplier)));
				}
			}
		});
		
		refreshBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CurrencyDownloader currencyDownloader = new CurrencyDownloader();
				currencyDownloader.execute("http://www.nbp.pl/kursy/xml/LastA.xml");
				downloading.setVisibility(View.VISIBLE);
			}
		});
		
		
		
	}

	public void onFromRadioButtonClicked(View view) {
		RadioButton radioButton = (RadioButton) view;
	    fromCurrency = currencies.get(radioButton.getText());
	}
	
	public void onToRadioButtonClicked(View view) {
		RadioButton radioButton = (RadioButton) view;
		toCurrency = currencies.get(radioButton.getText());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.calc, menu);
		return true;
	}
	
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
	
	
	@Override
	protected void onStop() {
		super.onStop();
		
	}
	
	

	private class CurrencyDownloader extends AsyncTask<String, Void, HashMap<String, Currency>> {
		
	    protected HashMap<String, Currency> doInBackground(String... url) {
	    	return CurrenciesParser.getCurrencies(url[0]);
	    }
	    
	    protected void onPostExecute(HashMap<String, Currency> result) {
	    	currencies = result;
	    	downloading.setVisibility(View.GONE);
	    }
	}

}
