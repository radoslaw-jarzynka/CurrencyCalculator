package com.orange.currencycalc;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author radoslawjarzynka
 * klasa obslugujaca komunikacje z baza danych SQLite
 */
public class DBAdapter {
	public static final String DEBUG_TAG = "SQLiteAdapter";
	
	private SQLiteDatabase db;
	private Context context;
	private DatabaseHelper dbHelper;
	
	//statyczne zmienne bedace nazwami bazy
	private static final int DB_VERSION = 1;
    private static final String DB_NAME = "currenciesDB.db";
    private static final String DB_CURRENCIES_TABLE = "currencies";
    
    //elementy bazy danych
    public static final String KEY_NAME = "name";
    public static final String NAME_OPTIONS = "TEXT PRIMARY KEY NOT NULL";
    public static final int NAME_COLUMN = 0;
    public static final String KEY_VALUE = "value";
    public static final String VALUE_OPTIONS = "REAL DEFAULT 0";
    public static final int VALUE_COLUMN = 1;
    public static final String KEY_MULTIPLIER = "multiplier";
    public static final String MULTIPLIER_OPTIONS = "REAL DEFAULT 0";
    public static final int MULTIPLIER_COLUMN = 2;
    
    //komendy SQL tworzace i niszczace baze
    private static final String DB_CREATE_TODO_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DB_CURRENCIES_TABLE + "( " +
            KEY_NAME + " " + NAME_OPTIONS + ", " +
            KEY_VALUE + " " + VALUE_OPTIONS + ", " +
            KEY_MULTIPLIER + " " + MULTIPLIER_OPTIONS +
            ");";
    private static final String DROP_TODO_TABLE =
            "DROP TABLE IF EXISTS " + DB_CURRENCIES_TABLE;
    
    /**
     * @param context kontekst aplikacji
     * 
     * konstruktor obiektu przypisujacy podany z zewnatrz obiekt context do wewnetrznego obiektu
     */
    public DBAdapter(Context context) {
        this.context = context;
    }
    
    /**
     * metoda otwierajaca polaczenie z baza
     * @return
     */
    public DBAdapter open(){
        dbHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLException e) {
            db = dbHelper.getReadableDatabase();
        }
        return this;
    }
    
    /**
     * metoda zamykajaca polaczenie z baza
     */
    public void close() {
        dbHelper.close();
    }
    
    /**
     * metoda wrzucajaca obiekt Currency do bazy danych. W wypadku gdy obiekt o takim samym kluczu juz jest w bazie zostanie on zamieniony
     * @param _cur obiekt Currency, ktory chcemy wsadzic do bazy
     * @return id wsadzonego obiektu
     */
    public long insertCurrency(Currency _cur) {
    	return insertCurrency(_cur.name,_cur.value,_cur.multiplier);
    }
    
    /**
     * metoda wrzucajaca do bazy danych podane ciagi znakow, przeciazenie metody insertCurrency(Currency _cur)
     * @param name nazwa waluty
     * @param value wartosc przelicznika
     * @param multiplier wartosc mnoznika
     * @return id wsadzonego obiektu
     */
    public long insertCurrency(String name, double value, double multiplier) {
    	db.execSQL("INSERT OR REPLACE INTO currencies VALUES ('"+name+"',"+value+","+multiplier+")");
    	return 0;
    }
    
    /**
     * metoda usuwajaca walute z bazy danych
     * @param name nazwa waluty
     * @return true jesli sie udalo, false jesli nie
     */
    public boolean deleteCurrency(String name){
        String where = KEY_NAME + "=" + name;
        return db.delete(DB_CURRENCIES_TABLE, where, null) > 0;
    }
    
    /** metoda zwracajacy kursor, dzieku ktoremu mozna przejrzec cala baze danych
     * @return kursor do bazy danych
     */
    public Cursor getAllCurrencies() {
        String[] columns = {KEY_NAME, KEY_VALUE, KEY_MULTIPLIER};
        return db.query(DB_CURRENCIES_TABLE, columns, null, null, null, null, null);
    }
     
    /** metoda pobierajaca informacje o danej walucie z bazy danych
     * @param name nazwa waluty
     * @return obiekt Currency reprezentujacy dana walute
     */
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
    
    /** metoda sprawdzajaca istnienie danej waluty w bazie danych
     * @param name nazwa waluty
     * @return true jezeli istnieje, false jezeli nie
     */
    public boolean doesCurrencyExistInDB(String name) {
    	try {
        Cursor cursor = db.rawQuery("Select * from currencies where name = " + name, null);
        if(cursor.getCount()<=0){
        	return false;
        }
        return true;
    	} catch (SQLiteException e){
    		return false;
    	}
    }
    
    /**
     * @author radoslawjarzynka
     * klasa wspomagajaca obsluge bazy danych, sluzaca do logowania wydarzen do LogCata
     */
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
