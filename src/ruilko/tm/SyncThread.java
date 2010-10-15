package ruilko.tm;

import java.net.URI;

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
	private String localUuid;
	private DbAccessor dbAccessor;
	private HttpGetter httpGetter;
	
	public SyncThread(Handler _handler, String _host, String _localUuid) {
		handler = _handler;
		host = _host;
		localUuid = _localUuid;
		
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
			String uuid = getRemoteUuid();
			inform(uuid, Events.FINISHED);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			inform("Error on syncing" + e.toString(), Events.FINISHED);
			e.printStackTrace();
		}
	}

	@Override
	public void inform(String msg) {
		inform(msg, Events.TEXT);
	}
	
	private String getRemoteUuid() throws Exception {
		String uuid;
		URI uri = new URI("http", host, "/get_uuid", null);
		String content = httpGetter.getResponse(uri, null, null);
		
		// Drill into the JSON response to find the content body
		JSONObject resp = (JSONObject) new JSONTokener(content).nextValue();
		uuid = resp.getString("uuid");

		inform("Server UUID is " + uuid, Events.TEXT);

		return uuid;
	}
}
