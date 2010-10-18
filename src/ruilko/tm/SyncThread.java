package ruilko.tm;

import java.net.URI;
import java.security.InvalidKeyException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SyncThread extends Thread implements MessageCallback {
	private static final String TAG = "SyncThread";
	
	private Handler handler;
	private String host;
	private int port;
	private String localUuid;
	private HttpGetter httpGetter;
	private DbAccessor dbAccessor;
	
	public SyncThread(DbAccessor _dbAccessor, Handler _handler, String _host, int _port, String _localUuid) {
		handler = _handler;
		host = _host;
		port = _port;
		localUuid = _localUuid;
		dbAccessor = _dbAccessor;
		
		httpGetter = new HttpGetter(this);
	}

	public enum Events {
		DEBUG, TEXT, FINISHED, ERROR
	}

	void inform(String _text, Events _event) {
		Message msg = handler.obtainMessage(_event.ordinal());
		Bundle bndl = new Bundle();
		bndl.putString("text", _text);
		msg.setData(bndl);
		handler.sendMessage(msg);
	}

	@Override
	public void run() {
//			// Drill into the JSON response to find the content body
//			JSONObject resp = (JSONObject) new JSONTokener(content.toString())
//					.nextValue();
//			// JSONObject query = resp.getJSONObject("uuid");
//			// JSONObject pages = query.getJSONObject("pages");
//			// JSONObject page = pages.getJSONObject((String)
//			// pages.keys().next());
//			// JSONArray revisions = page.getJSONArray("revisions");
//			// JSONObject revision = revisions.getJSONObject(0);
//			String uuid = resp.getString("uuid");
//			inform(uuid, Events.FINISHED);
//		} catch (IOException e) {
//			Log.e(TAG, e.toString());
//			inform("Error on syncing" + e.toString(), Events.FINISHED);
//			e.printStackTrace();
		try {
			dbAccessor.beginTransaction();
	        Long now = Long.valueOf(System.currentTimeMillis());
	        Log.d(TAG, "Sync started at " + now.toString());

	        // Get remote Uuid and it's lastUpdated time
	        Pair<String, Long> remoteInfo = getRemoteInfo();
			inform(remoteInfo.getFirst(), Events.TEXT);
			Long lastUpdated = dbAccessor.getLastUpdated(remoteInfo.getFirst());
			inform(lastUpdated.toString(), Events.TEXT);
			
			// TODO Upload local updates

			// Download remote updates
			processRemoteUpdates(lastUpdated);

			dbAccessor.setTransactionSuccessful();			

			inform("Finished", Events.FINISHED);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			inform("Error on syncing " + e.toString(), Events.ERROR);
			e.printStackTrace();
		} finally {
			dbAccessor.endTransaction();
		}
	}

	@Override
	public void inform(String msg) {
		inform(msg, Events.TEXT);
	}
	
	/**
	 * @return pair<uuid of server, last updated time on server - i.e. FROM time for uploading local tasks>
	 * @throws Exception
	 */
	private Pair<String, Long> getRemoteInfo() throws Exception {
		String uuid;
		URI uri = new URI("http", null, host, port, "/get_uuid", null, null);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put( "Uuid", localUuid );
		String content = httpGetter.getResponse(uri, headers, null);
		
		// Drill into the JSON response to find the content body
		JSONObject resp = (JSONObject) new JSONTokener(content).nextValue();
		uuid = resp.getString("uuid");
		Long lastUpdated = resp.getLong("lastUpdated");

		inform("Server UUID:last updated" + uuid + ":" + lastUpdated.toString(), Events.TEXT);

		Pair<String, Long> result = new Pair<String, Long>(uuid, lastUpdated);
		return result;
	}

	private void processRemoteUpdates(Long fromTime) throws Exception {
		URI uri = new URI("http", null, host, port, "/get_updates", "fromTime="+fromTime.toString(), null);
		String content = httpGetter.getResponse(uri, null, null);
		
		// Drill into the JSON response to find the content body
		JSONObject updates = (JSONObject) new JSONTokener(content).nextValue();
		Log.d(TAG, "Got JSON from server:");
		Log.d(TAG, updates.toString());
		if( updates.has("tasks") ) {
			JSONArray tasks = updates.getJSONArray("tasks");
			for(int i=0; i<tasks.length(); ++i) {
				JSONObject json = tasks.getJSONObject(i);
				Log.d(TAG, "get task JSON");
				Log.d(TAG, json.toString());
				
				Task task = new Task();
				task.fromJson(json);
				updateTask(task);
			}
		}
	}
	
	private void updateTask(Task task) {
		boolean needUpdate = false;
		Task remote = new Task(task);
		try {
			dbAccessor.getDbObject(task, task.getUuid());
		} catch (InvalidKeyException e) {
			Log.d(TAG, "There are no local task " + task.getUuid());
			needUpdate = true;
		}
		if( !needUpdate && remote.getGlobalUpdated()>task.getGlobalUpdated() ) {
			needUpdate = true;
			Log.d(TAG, "Local task is too old - " + task.getUuid());
		}
		
		if( needUpdate ) {
			Log.d(TAG, "Should update task " + remote.getUuid());
			dbAccessor.setDbObject(remote);
		}
		else
		{
			Log.d(TAG, "Should NOT update task " + remote.getUuid());
			Log.d(TAG, "Local task - " + task.toString());
		}
	}
}
