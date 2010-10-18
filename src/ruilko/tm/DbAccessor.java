package ruilko.tm;

import java.security.InvalidKeyException;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAccessor {
	private static final String TAG = "DbAccessor";

	private class DbSqlite extends SQLiteOpenHelper {
		static final String DATABASE_NAME = "tm.sqlite";
		static final int DATABASE_VERSION = 1;

		DbSqlite(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * The timestamp for when the note was created
		 * Type: INTEGER (long from System.curentTimeMillis())
		 * Long now = Long.valueOf(System.currentTimeMillis());
		 */

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Will create TM database");
			
			db.execSQL("CREATE TABLE Tasks (uuid text PRIMARY KEY,"
					+ "parentUuid text,notes TEXT,"
					+ "title TEXT,localUpdated INTEGER,globalUpdated INTEGER" + ");");
			db.execSQL("CREATE TABLE Activities (uuid text PRIMARY KEY,"
					+ "taskUuid text,startTime INTEGER,"
					+ "title TEXT,localUpdatedINTEGER,globalUpdated INTEGER" + ");");
			db.execSQL("CREATE TABLE Hosts (" + "uuid text PRIMARY KEY,"
					+ "lastUpdated INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	private DbSqlite dbAccessor;
	private SQLiteDatabase db;

	public DbAccessor(Context context) {
		dbAccessor = new DbSqlite(context);
		db = dbAccessor.getWritableDatabase();
	}

	public void close() {
		dbAccessor.close();
	}

	public Long getLastUpdated(String _uuid) {
		Long result;

		Cursor c = db.query("Hosts", null, "lastUpdated", null, null, null, null, null);
		if (c.moveToFirst() == false)
			result = (long) 0;
		else
			result = c.getLong(1);

		c.close();
		return result;
	}
	
	public void getDbObject(DbObject obj, String uuid) throws InvalidKeyException {
		String[] where = {uuid};
		ArrayList<String> fieldsList = obj.getSelectFields();
		String[] fields = new String[fieldsList.size()];
	    fields = fieldsList.toArray(fields);
	    
		Cursor c = db.query(obj.getTable(), fields, obj.getKeyField()+ "= ?", where, null, null, null);
		if (c.moveToFirst() == false) {
			c.close();
			throw new InvalidKeyException("No such object: table " + obj.getTable() + ", key - " + obj.getKeyField() + "=" + uuid);
		}
		
		obj.fillFromDb(c);

		c.close();
	}

	public void setDbObject(DbObject obj) {
		db.replace(obj.getTable(), "", obj.fillDb());
	}

	public void beginTransaction() {
		db.beginTransaction();
	}

	public void setTransactionSuccessful() {
		db.setTransactionSuccessful();
	}

	public void endTransaction() {
		db.endTransaction();
	}
}