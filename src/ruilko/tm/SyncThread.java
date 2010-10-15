package ruilko.tm;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;

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
		DEBUG, TEXT, FINISHED
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
	        Long now = Long.valueOf(System.currentTimeMillis());

	        // Get remote Uuid and it's lastUpdated time
	        Pair<String, Long> remoteInfo = getRemoteInfo();
			inform(remoteInfo.getFirst(), Events.TEXT);
			Long lastUpdated = dbAccessor.getLastUpdated(remoteInfo.getFirst());
			inform(lastUpdated.toString(), Events.TEXT);
			
			// TODO Upload local updates

			// Download remote updates
			getRemoteUpdates(lastUpdated);
			
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			inform("Error on syncing " + e.toString(), Events.FINISHED);
			e.printStackTrace();
		}
		inform("Finished", Events.FINISHED);
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

	private String getRemoteUpdates(Long fromTime) throws Exception {
		String uuid = "123";
		URI uri = new URI("http", null, host, port, "/get_updates", "fromTime="+fromTime.toString(), null);
		String content = httpGetter.getResponse(uri, null, null);
		
		// Drill into the JSON response to find the content body
		JSONObject updates = (JSONObject) new JSONTokener(content).nextValue();
		Log.d(TAG, "Got JSON from server:");
		Log.d(TAG, updates.toString());
		if( updates.has("tasks") ) {
			JSONArray tasks = updates.getJSONArray("tasks");
			for(int i=0; i<tasks.length(); ++i) {
				JSONObject task = tasks.getJSONObject(i);
				Log.d(TAG, "get task JSON");
			}
		}

		return uuid;
	}
}
