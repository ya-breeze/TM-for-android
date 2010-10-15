package ruilko.tm;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbAccessor { 

	private class DbSqlite extends SQLiteOpenHelper {
		static final String DATABASE_NAME = "/sdcard/.tm.sqlite";
		static final int DATABASE_VERSION = 1;
	
		DbSqlite(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }
        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
        Long now = Long.valueOf(System.currentTimeMillis());
         */

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL("CREATE TABLE Tasks (" +
	        		"uuid text PRIMARY KEY," +
	        		"parentUuid text," +
	                "title TEXT," +
	                "note TEXT," +
	                "localChanged INTEGER," +
	                "globalChanged INTEGER" +
	                ");");
	        db.execSQL("CREATE TABLE Activities (" +
	        		"uuid text PRIMARY KEY," +
	        		"taskUuid text," +
	                "startTime INTEGER," +
	                "localChanged INTEGER," +
	                "globalChanged INTEGER" +
	                ");");
	        db.execSQL("CREATE TABLE Hosts (" +
	        		"uuid text PRIMARY KEY," +
	                "lastUpdated INTEGER" +
	                ");");
	    }
	
	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    }
	}

	private DbSqlite dbAccessor;
	
	public Long getLastUpdated(String _uuid) {
		SQLiteDatabase db = dbAccessor.getReadableDatabase();
		Cursor c = db.query("Hosts", null, "lastUpdated", null, null, null, null, null);
		Long result = c.getLong(1);
		c.close();
		return result;		
	}
}