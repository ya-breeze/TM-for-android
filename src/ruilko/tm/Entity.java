package ruilko.tm;

import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public abstract class Entity implements DbObject {
	public Entity() {
		this.uuid = UUID.randomUUID().toString();
	}

	public Entity(Entity task) {
		uuid = task.uuid;
		localUpdated = task.localUpdated;
		globalUpdated = task.globalUpdated;
		title = task.title;
	}

	public void touch() {
		this.localUpdated = Long.valueOf(System.currentTimeMillis());
		this.globalUpdated = this.localUpdated;
	}

	public void fromJson(JSONObject json) {
		try {
			if (json.has("uuid"))
				this.uuid = json.getString("uuid");
			if (json.has("localUpdated"))
				this.localUpdated = json.getLong("localUpdated");
			if (json.has("globalUpdated"))
				this.globalUpdated = json.getLong("globalUpdated");
			if (json.has("title"))
				this.title = json.getString("title");
		} catch (JSONException e) {
			Log.e(TAG,
					"Bad JSON object " + e.toString() + " - '"
							+ json.toString() + "'");
			e.printStackTrace();
		}
	}

	@Override
	public ContentValues fillDb() {
		ContentValues result = new ContentValues();

		result.put("uuid", this.uuid);
		result.put("localUpdated", this.localUpdated);
		result.put("globalUpdated", this.globalUpdated);
		result.put("title", this.title);

		return result;
	}

	@Override
	public String getKeyField() {
		return "uuid";
	}

	@Override
	public ArrayList<String> getSelectFields() {
		ArrayList<String> result = new ArrayList<String>();
		result.add("uuid");
		result.add("localUpdated");
		result.add("globalUpdated");
		result.add("title");
		return result;
	}

	@Override
	public void fillFromDb(Cursor c) {
		this.uuid = c.getString(0);
		this.localUpdated = c.getLong(1);
		this.globalUpdated = c.getLong(2);
		this.title = c.getString(3);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getLocalUpdated() {
		return localUpdated;
	}

	public void setLocalUpdated(Long localUpdated) {
		this.localUpdated = localUpdated;
	}

	public Long getGlobalUpdated() {
		return globalUpdated;
	}

	public void setGlobalUpdated(Long globalUpdated) {
		this.globalUpdated = globalUpdated;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	public String getTitle() {
		return title;
	}

	private String uuid;
	private Long localUpdated;
	private Long globalUpdated;
	private String title;

	private static final String TAG = "Entity";
}
