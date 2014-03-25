package com.orange.currencycalc;
import java.io.Serializable;
import java.util.List;
 
import com.google.api.client.util.Key;
 
/**
 * @author radoslawjarzynka
 *	klasa przechowujaca cala liste obiektow typu Bank przeslana przez Google API w formacie JSON
 */
public class BankList implements Serializable {
 
	private static final long serialVersionUID = 1L;

	@Key
    public String status;
 
    @Key
    public List<Bank> results;
 
}