package com.orange.currencycalc;
import java.io.Serializable;
import java.util.List;
 
import com.google.api.client.util.Key;
 
public class BankList implements Serializable {
 
    @Key
    public String status;
 
    @Key
    public List<Bank> results;
 
}