package com.orange.currencycalc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	public static final String DEBUG_TAG = "SQLiteAdapter";
	
	private SQLiteDatabase db;
	private Context context;
	private DatabaseHelper dbHelper;
	
	private static final int DB_VERSION = 1;
    private static final String DB_NAME = "currenciesDB.db";
    private static final String DB_CURRENCIES_TABLE = "currencies";
    
    
    public static final String KEY_NAME = "name";
    public static final String NAME_OPTIONS = "TEXT PRIMARY KEY NOT NULL";
    public static final int NAME_COLUMN = 0;
    public static final String KEY_VALUE = "value";
    public static final String VALUE_OPTIONS = "REAL DEFAULT 0";
    public static final int VALUE_COLUMN = 1;
    public static final String KEY_MULTIPLIER = "multiplier";
    public static final String MULTIPLIER_OPTIONS = "REAL DEFAULT 0";
    public static final int MULTIPLIER_COLUMN = 2;
    
    private static final String DB_CREATE_TODO_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DB_CURRENCIES_TABLE + "( " +
            KEY_NAME + " " + NAME_OPTIONS + ", " +
            KEY_VALUE + " " + VALUE_OPTIONS + ", " +
            KEY_MULTIPLIER + " " + MULTIPLIER_OPTIONS +
            ");";
    private static final String DROP_TODO_TABLE =
            "DROP TABLE IF EXISTS " + DB_CURRENCIES_TABLE;
    
    public DBAdapter(Context context) {
        this.context = context;
    }
    
    public DBAdapter open(){
        dbHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLException e) {
            db = dbHelper.getReadableDatabase();
        }
        return this;
    }
    
    public void close() {
        dbHelper.close();
    }
    
    public long insertCurrency(Currency _cur) {
    	return insertCurrency(_cur.name,_cur.value,_cur.multiplier);
    }
    
    public long insertCurrency(String name, double value, double multiplier) {
    	try {
        ContentValues newCurrencyValues = new ContentValues();
        newCurrencyValues.put(KEY_NAME, name);
        newCurrencyValues.put(KEY_VALUE, value);
        newCurrencyValues.put(KEY_MULTIPLIER, multiplier);
        return db.insert(DB_CURRENCIES_TABLE, null, newCurrencyValues);
    	} catch (SQLException e) {
    		db.execSQL("DELETE FROM currencies WHERE name = " + name);
            ContentValues newCurrencyValues = new ContentValues();
            newCurrencyValues.put(KEY_NAME, name);
            newCurrencyValues.put(KEY_VALUE, value);
            newCurrencyValues.put(KEY_MULTIPLIER, multiplier);
            return db.insert(DB_CURRENCIES_TABLE, null, newCurrencyValues);
    	}
    }
    
    public boolean updateCurrency(Currency currency) {
        //long id = currency.getId();
        String name = currency.getName();
        double value = currency.getValue();
        double multiplier = currency.getMultiplier();
        return updateCurrency(name, value, multiplier);
    }
     
    public boolean updateCurrency(String name, double value, double multiplier) {
    	try {
        String where = KEY_NAME + "=" + name;
        ContentValues updateCurrencyValues = new ContentValues();
        updateCurrencyValues.put(KEY_VALUE, value);
        updateCurrencyValues.put(KEY_MULTIPLIER, multiplier);
        return db.update(DB_CURRENCIES_TABLE, updateCurrencyValues, where, null) > 0;
    	} catch (SQLiteException e) {
    		//db.execSQL("DELETE FROM currencies WHERE name = " + name);
    		insertCurrency(name, value, multiplier);
    		return true;
    	}
    }

    public boolean deleteCurrency(String name){
        String where = KEY_NAME + "=" + name;
        return db.delete(DB_CURRENCIES_TABLE, where, null) > 0;
    }
    
    public Cursor getAllCurrencies() {
        String[] columns = {KEY_NAME, KEY_VALUE, KEY_MULTIPLIER};
        return db.query(DB_CURRENCIES_TABLE, columns, null, null, null, null, null);
    }
     
    public Currency getCurrency(String name) {
        String[] columns = {KEY_NAME, KEY_VALUE, KEY_MULTIPLIER};
        String where = KEY_NAME + "=" + name;
        Cursor cursor = db.query(DB_CURRENCIES_TABLE, columns, where, null, null, null, null);
        Currency _currency = null;
        if(cursor != null && cursor.moveToFirst()) {
            double value = cursor.getDouble(VALUE_COLUMN);
            double multiplier = cursor.getDouble(MULTIPLIER_COLUMN);
            _currency = new Currency(name, value, multiplier);
        }
        return _currency;
    }
    
    public boolean doesCurrencyExistInDB(String name) {
    	String Query = "Select * from currencies where name = " + name ;
        Cursor cursor = db.rawQuery("Select * from currencies where name = " + name, null);
        if(cursor.getCount()<=0){
        	return false;
        }
        return true;
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
     
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_TODO_TABLE);
     
            Log.d(DEBUG_TAG, "Database creating...");
            Log.d(DEBUG_TAG, "Table " + DB_CURRENCIES_TABLE + " ver." + DB_VERSION + " created");
        }
     
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TODO_TABLE);
     
            Log.d(DEBUG_TAG, "Database updating...");
            Log.d(DEBUG_TAG, "Table " + DB_CURRENCIES_TABLE + " updated from ver." + oldVersion + " to ver." + newVersion);
            Log.d(DEBUG_TAG, "All data is lost.");
     
            onCreate(db);
        }
    }
    
    
}
