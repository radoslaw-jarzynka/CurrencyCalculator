package com.orange.currencycalc;

import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

public class CurrenciesParser {

	public static final String TAG = CurrenciesParser.class.getName();

	public static HashMap<String, Currency> getCurrencies(String urlString, HashMap<String,Currency> currencies) {
		//HashMap<String, Currency> currencies = new HashMap<String, Currency>();

		try {

			URL url = new URL(urlString);

			InputSource input = new InputSource(url.openStream());

			DocumentBuilderFactory dbf = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(input);

			doc.getDocumentElement().normalize();

			NodeList nodeList = doc.getElementsByTagName("pozycja");

			int length = nodeList.getLength();
			for (int i = 0; i < length; i++) {

				Node node = nodeList.item(i);
				String name = getValue((Element) node, "kod_waluty");
				double value = Double.parseDouble(getValue((Element) node,
						"kurs_sredni").replace(",", "."));
				double multiplier = Double.parseDouble(getValue(
						(Element) node, "przelicznik").replace(",", "."));
				if (name !=null) currencies.put(name, new Currency(name, value, multiplier));
			}

		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}

		currencies.put("PLN", new Currency("PLN", 1, 1));
		return currencies;
	}
	
	
	private static String getValue(Element element, String tag) {
		NodeList kodList = element.getElementsByTagName(tag);
		Element kodElement = (Element) kodList.item(0);
		return kodElement.getFirstChild().getNodeValue();
	}
}