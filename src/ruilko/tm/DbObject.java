package ruilko.tm;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;

public interface DbObject {
	public String getTable();
	public ArrayList<String> getSelectFields();
	public String getKeyField();
	public void fillFromDb(Cursor c);
	public ContentValues fillDb();
}
