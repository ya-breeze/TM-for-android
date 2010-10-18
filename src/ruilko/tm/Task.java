package ruilko.tm;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class Task extends Entity {
	public Task() {
		super();
		parentUuid = "00000000-0000-0000-0000-000000000000";
	}
	
	public Task(Task task) {
		super(task);
		this.notes = task.notes;
		this.parentUuid = task.parentUuid;
	}

	@Override
	public void fromJson(JSONObject json) {
		try {
			if (json.has("parentUuid"))
				this.parentUuid = json.getString("parentUuid");
			if (json.has("notes"))
				this.notes = json.getString("notes");
		} catch (JSONException e) {
			Log.e(TAG, "Bad JSON object " + e.toString() + " - '" + json.toString() + "'");
			e.printStackTrace();
		}

		super.fromJson(json);
	}

	@Override
	public ContentValues fillDb() {
		ContentValues result = super.fillDb();
		
		result.put("parentUuid", this.parentUuid);
		result.put("notes", this.notes);
		
		return result;
	}

	@Override
	public ArrayList<String> getSelectFields() {
		ArrayList<String> result = super.getSelectFields();
		result.add("parentUuid");
		result.add("notes");
		return result;
	}

	@Override
	public void fillFromDb(Cursor c) {
		super.fillFromDb(c);
		this.notes = c.getString( c.getColumnIndex("notes") );
		this.parentUuid = c.getString( c.getColumnIndex("parentUuid") );
	}
	
	public String toString() {
		String result = "uuid:" + getUuid();
		result += "; parentUuid:" + getParentUuid();
		result += "; title:" + getTitle();
		result += "; notes:" + getNotes();
		result += "; localUpdated:" + getLocalUpdated();
		result += "; globalUpdated:" + getGlobalUpdated();
		
		return result;
	}


	public String getParentUuid() {
		return parentUuid;
	}

	public void setParentUuid(String parentUuid) {
		this.parentUuid = parentUuid;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	private String parentUuid;
	private String notes;

	private static final String TAG = "Task";

	@Override
	public String getTable() {
		return "Tasks";
	}
}
