package com.orange.currencycalc;

 
import java.io.Serializable;
 
import com.google.api.client.util.Key;


/**
 * @author radoslawjarzynka
 *	klasa przechowujaca informacje o poszczegolnym banku w postaci "klucz"-"wartosc"
 *	nazwy obiektow pokrywaja sie z nazwami atrybutow przekazywanych w JSON z Google Places API
 */
public class Bank implements Serializable {

	private static final long serialVersionUID = 1L;

	@Key
    public String id;
     
    @Key
    public String name;
     
    @Key
    public String reference;
     
    @Key
    public String icon;
     
    @Key
    public String vicinity;
     
    @Key
    public Geometry geometry;
     
    @Key
    public String formatted_address;
     
    @Key
    public String formatted_phone_number;
 
    @Override
    public String toString() {
        return name + " - " + id + " - " + reference;
    }
     
    /**
     * @author radoslawjarzynka
     *klasa przechowujaca informacje o lokalizacji obiektu
     */
    public static class Geometry implements Serializable
    {
		private static final long serialVersionUID = 1L;
		@Key
        public Location location;
    }
     
    /**
     * @author radoslawjarzynka
     *	klasa przechowujaca dokladna wartosc szerokosci i dlugosci geograficznej obiektu
     */
    public static class Location implements Serializable
    {
		private static final long serialVersionUID = 1L;

		@Key
        public double lat;
         
        @Key
        public double lng;
    }
     
}